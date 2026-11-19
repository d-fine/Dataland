package org.dataland.datalandcompanyservice.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Basic OpenAPI configuration of the company service. The service is internal only, so the OpenAPI specification is
 * only used for client generation and no Swagger UI is exposed.
 */
@OpenAPIDefinition(
    info =
        Info(
            title = "Dataland company service API documentation",
            version = "1.0.0",
        ),
    servers = [Server(url = "/company-service")],
)
@SecurityScheme(
    name = "default-bearer-auth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
)
interface OpenAPIConfiguration
