package org.dataland.datalandbackend

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@SecurityScheme(
    name = "default-auth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER
)
class DatalandBackend

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    runApplication<DatalandBackend>(*args)
}
