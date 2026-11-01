package org.dataland.datalandproxyfinder.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic OpenAPI configuration of the proxy finder. The service is internal only, so the OpenAPI specification is
 * only used for client generation and no Swagger UI is exposed.
 */
@OpenAPIDefinition(
    info =
        Info(
            title = "Dataland proxy finder API documentation",
            version = "1.0.0",
        ),
    servers = [Server(url = "/proxy-finder")],
)
@SecurityScheme(
    name = "default-bearer-auth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
)
interface OpenAPIConfiguration
