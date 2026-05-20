package com.example.oauth_client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.web.client.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2Login(oauth ->
                        oauth.tokenEndpoint(Customizer.withDefaults())
                                .authorizationEndpoint(Customizer.withDefaults())
                                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService()))
                                .redirectionEndpoint(Customizer.withDefaults())
                )
                .build();
    }


    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();
        idTokenDecoderFactory.setJwsAlgorithmResolver((_) -> SignatureAlgorithm.RS256);
        return idTokenDecoderFactory;
    }


    public JwtDecoder jwtDecoder(ClientRegistration clientRegistration) {
        return idTokenDecoderFactory().createDecoder(clientRegistration);
    }

    @Bean
    public DefaultOAuth2UserService customOAuth2UserService() {
        return new DefaultOAuth2UserService() {

            private Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter = (
                    _) -> (attributes) -> attributes;

            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                Assert.notNull(userRequest, "userRequest cannot be null");
                Jwt jwt = jwtDecoder(userRequest.getClientRegistration()).decode(userRequest.getAdditionalParameters().get("id_token").toString());
                String userNameAttributeName = "given_name";
                OAuth2AccessToken token = userRequest.getAccessToken();
                Map<String, Object> attributes = this.attributesConverter.convert(userRequest).convert(jwt.getClaims());
                Collection<GrantedAuthority> authorities = getAuthorities(token, attributes, userNameAttributeName);
                return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);
            }

            public void setAttributesConverter(
                    Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
                Assert.notNull(attributesConverter, "attributesConverter cannot be null");
                this.attributesConverter = attributesConverter;
            }

            private Collection<GrantedAuthority> getAuthorities(OAuth2AccessToken token, Map<String, Object> attributes,
                                                                String userNameAttributeName) {
                Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
                authorities.add(new OAuth2UserAuthority(attributes, userNameAttributeName));
                for (String authority : token.getScopes()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
                }
                return authorities;
            }
        };
    }
}
