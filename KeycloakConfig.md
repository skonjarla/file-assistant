# 🔑 Keycloak Configuration Guide for File Assistant

## Overview
Keycloak provides authentication and authorization for the File Assistant MCP Server and Client. This document details the configuration steps and account setup required for secure access.

> **Keycloak Realm**: The configuration in this guide uses the realm named "home".

## Client Configuration

### 1. MCP File Client (`mcpfileclient`)

#### 1.1 Basic Configuration
This client handles JWT token validation and client application authentication.

![Keycloak MCP File Client Configuration](keycloak_mcpfileclient_config_1.png)

**Configuration Steps**:
1. **Client ID**: Set to `mcpfileclient`
2. **Client Protocol**: `openid-connect`
3. **Access Type**: `confidential` (for server-side applications)
4. **Standard Flow Enabled**: `ON`
5. **Direct Access Grants Enabled**: `ON` (for testing purposes)
6. **Service Accounts Enabled**: `ON` (for server-to-server communication)
7. **Authorization Enabled**: `ON`
8. **Save the configuration**

#### 1.2 URL Configuration
Configure the following settings for your File Assistant MCP Client application:

![Keycloak Client URL Configuration](keycloak_mcpfileclient_config_2.png)

**Configuration Steps**:
1. **Valid Redirect URIs**: 
   - Add your application's URLs (e.g., `http://localhost:3000/*`)
   - Include any additional environments (dev, staging, production)
   - Use wildcards for subpaths if needed

2. **Web Origins**: 
   - Add allowed origins (e.g., `http://localhost:3000`)
   - Include protocol, domain, and port
   - Use `*` for development only (not recommended for production)

3. **Admin URL**: (Optional) Set if using admin console integration

> **Development Note**: The example shows configurations for ports 3000, 8383, and 8585. Adjust these according to your development environment.

### 2. MCP Client (`mcpclient`)
This client is used during application startup to obtain MCP Server details.

#### 2.1 Basic Configuration
![MCP Client Configuration 1](keycloak_mcpclient_config_1.png)

**Configuration Steps**:
1. **Client ID**: Set to `mcpclient`
2. **Client Protocol**: `openid-connect`
3. **Access Type**: `confidential`
4. **Service Accounts Enabled**: `ON` (required for client credentials flow)
5. **Standard Flow Enabled**: `OFF` (not needed for service-to-service)

#### 2.2 Credentials Configuration
![MCP Client Configuration 2](keycloak_mcpclient_config_2.png)

**Configuration Steps**:
1. Navigate to the `Credentials` tab
2. Note the `Client Secret` (you'll need this for application configuration)
3. Optionally rotate the secret if needed
4. Set token expiration policies as required

#### 2.3 Client Configuration Import
Refer to mcpclient.json and mcpfileclient.json for the complete client configurations. These can be imported into via Keycloak's admin console.
## Application Configuration

### 3. Spring Boot Application Properties
Add the following to your `application.properties`:

```properties
# Keycloak OAuth2 Configuration
spring.security.oauth2.client.provider.authserver.issuer-uri=http://localhost:9090/realms/home
spring.security.oauth2.client.registration.authserver-client-credentials.client-id=mcpclient
spring.security.oauth2.client.registration.authserver-client-credentials.client-secret=your_client_secret
spring.security.oauth2.client.registration.authserver-client-credentials.authorization-grant-type=client_credentials

# MCP Client Configuration
spring.ai.mcp.client.enabled=true
spring.ai.mcp.client.name=file-assistant-client
spring.ai.mcp.client.version=1.0.0
```

## Security Best Practices

1. **Production Deployment**:
   - Always use HTTPS in production
   - Configure proper CORS policies
   - Set appropriate token expiration times

2. **Secret Management**:
   - Never commit client secrets to version control
   - Use environment variables or secret management tools
   - Rotate client secrets regularly

3. **Access Control**:
   - Implement proper role-based access control
   - Follow the principle of least privilege
   - Regularly audit user permissions

4. **Monitoring**:
   - Enable Keycloak's event logging
   - Monitor failed login attempts
   - Set up alerts for suspicious activities

## Troubleshooting

### Common Issues and Solutions

1. **Authentication Failures**:
   - Verify client ID and secret match exactly
   - Check that the realm name is correct (case-sensitive)
   - Ensure the user has the correct roles assigned

2. **Redirect URI Mismatch**:
   - Check that the redirect URI in the request matches exactly with the configured URI
   - Include trailing slashes if used in the configuration

3. **CORS Issues**:
   - Verify that the Web Origins are correctly configured
   - Check for protocol mismatches (http vs https)
   - Ensure the port numbers match exactly

4. **Token Validation Issues**:
   - Verify the issuer URL is accessible
   - Check that the token signature is valid
   - Ensure the token hasn't expired

For additional help, refer to the [Keycloak Documentation](https://www.keycloak.org/documentation) or check the server logs for detailed error messages.