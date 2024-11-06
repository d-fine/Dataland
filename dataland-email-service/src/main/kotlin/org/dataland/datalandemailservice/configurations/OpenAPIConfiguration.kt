package org.dataland.datalandemailservice.configurations

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

/**
 * Basic configuration for the OpenAPI Swagger-UI available at /email/swagger-ui/index.html
 */
@OpenAPIDefinition(
    info =
        Info(
            title = "Dataland Email Service API documentation",
            version = "1.0.0",
        ),
    servers = [Server(url = "/email")],
)
@Configuration
interface OpenAPIConfiguration
