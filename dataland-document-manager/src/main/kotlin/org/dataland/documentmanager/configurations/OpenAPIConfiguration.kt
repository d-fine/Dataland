package org.dataland.documentmanager.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /documents/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info = Info(
        title = "Dataland Document Manager API documentation",
        version = "1.0.0",
    ),
    servers = [Server(url = "/documents")],
)
@SecurityScheme(
    name = "default-bearer-auth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
)
@SecurityScheme(
    name = "default-oauth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        authorizationCode = OAuthFlow(
            authorizationUrl = "/keycloak/realms/datalandsecurity/protocol/openid-connect/auth",
            tokenUrl = "/keycloak/realms/datalandsecurity/protocol/openid-connect/token",
        ),
    ),
)
interface OpenAPIConfiguration
