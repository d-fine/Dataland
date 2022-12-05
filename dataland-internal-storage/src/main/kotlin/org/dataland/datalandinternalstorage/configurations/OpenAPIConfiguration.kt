package org.dataland.datalandinternalstorage.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /internal-storage/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info = Info(
        title = "Dataland internal storage API documentation",
        version = "1.0.0"
    ),
    servers = [Server(url = "/internal-storage")]
)
interface OpenAPIConfiguration
