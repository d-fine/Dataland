package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /api/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info = Info(
        title = "Dataland Backend API documentation"
    ),
    servers = [Server(url = "/api")]
)
class OpenAPIConfiguration
