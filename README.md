# Telegram OAuth2 Client with Spring Boot

A Spring Boot implementation of an OAuth2 Client specifically tailored for Telegram OAuth. This project demonstrates how to integrate Telegram authentication into a Spring Security-enabled application, including custom token decoding and user attribute extraction.

## Features

- Spring Security 6+ Integration: Leverages the latest Spring Security features for OAuth2 login.
- Telegram Specific Configuration: Pre-configured provider and registration for Telegram's OAuth2 implementation.
- Custom ID Token Decoding: Implements a custom OAuth2UserService to decode Telegram's id_token and map user attributes (like given_name).
- Secure Endpoints: Demonstrates public vs. protected route management.

## Tech Stack

- Java: 25
- Framework: Spring Boot 4.0.6 (Spring Web, Spring Security, OAuth2 Client)
- Build Tool: Gradle (Kotlin DSL)

## Prerequisites

1. Java 25: Ensure you have the latest JDK installed.
2. Telegram Bot: 
   - Create a bot via @BotFather.
   - Open the bot's settings, navigate Login Widget and copy the Client ID and Client Secret.
   - Enable "Allow Groups" or "Direct Login" as needed in the Bot Settings.
3. Domain/Tunnel: Telegram OAuth requires a publicly accessible domain (HTTPS). Localhost is generally not supported for redirects.

## Configuration

The application uses environment variables for flexible configuration. You can set these in your shell or a .env file.

| Variable | Description | Default |
|----------|-------------|---------|
| REDIRECT_URL | Your public base URL (e.g., https://my-app.com) | N/A |
| CLIENT_ID | Your Telegram Bot ID | 123456789 |
| CLIENT_SECRET | Your Telegram Bot Token/Secret | Fhjksad... |

### Example application.yaml snippet:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          telegram:
            redirect-uri: ${REDIRECT_URL}/login/oauth2/code/telegram
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
```

## Running Locally

Since Telegram requires a public URL, it is highly recommended to use a Cloudflare Tunnel or ngrok.

1. Start your tunnel:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```
2. Set your environment variables:
   ```bash
   export REDIRECT_URL=https://your-tunnel-url.trycloudflare.com
   export CLIENT_ID=your_bot_id
   export CLIENT_SECRET=your_bot_secret
   ```
3. Build and Run:
   ```bash
   ./gradlew bootRun
   ```

## Usage

- Home Page: http://localhost:8080/ (Publicly accessible)
- Protected Page: http://localhost:8080/secret (Redirects to Telegram Login)

Once authenticated, you will be redirected back to the /secret endpoint and see the "LESSSGOOOO!" message.

---
Built using Spring Boot and Java 25.
