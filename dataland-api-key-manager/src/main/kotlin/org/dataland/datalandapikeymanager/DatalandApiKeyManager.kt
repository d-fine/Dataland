package org.dataland.datalandapikeymanager

import org.dataland.datalandapikeymanager.configurations.OpenAPIConfiguration
import org.dataland.datalandbackendutils.configurations.DefaultResponseSchemaCustomizer
import org.dataland.datalandbackendutils.configurations.RequestRejectedExceptionHandler
import org.dataland.datalandbackendutils.controller.advice.KnownErrorControllerAdvice
import org.dataland.datalandbackendutils.controller.advice.UnknownErrorControllerAdvice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@Import(
    value = [
        KnownErrorControllerAdvice::class,
        UnknownErrorControllerAdvice::class,
        DefaultResponseSchemaCustomizer::class,
        RequestRejectedExceptionHandler::class
    ]
)
class DatalandApiKeyManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland API key manager process
 */
fun main(args: Array<String>) {
    runApplication<DatalandApiKeyManager>(args = args)
}
