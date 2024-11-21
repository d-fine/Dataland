package org.dataland.datalandspecificationservice.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /specifications/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info =
        Info(
            title = "Dataland Specification Service API documentation",
            version = "1.0.0",
        ),
    servers = [Server(url = "/specifications")],
)
interface OpenAPIConfiguration
