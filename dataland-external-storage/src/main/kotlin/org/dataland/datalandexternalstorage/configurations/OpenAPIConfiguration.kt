package org.dataland.datalandexternalstorage.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /external-storage/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info =
        Info(
            title = "Dataland external storage API documentation",
            version = "1.0.0",
        ),
    servers = [Server(url = "/external-storage")],
)
interface OpenAPIConfiguration
