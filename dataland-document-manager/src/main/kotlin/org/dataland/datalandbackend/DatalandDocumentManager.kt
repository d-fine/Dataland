package org.dataland.datalandbackend

import org.dataland.datalandbackend.configurations.OpenAPIConfiguration
import org.dataland.datalandbackendutils.configurations.DefaultResponseSchemaCustomizer
import org.dataland.datalandbackendutils.configurations.RequestRejectedExceptionHandler
import org.dataland.datalandbackendutils.controller.advice.KnownErrorControllerAdvice
import org.dataland.datalandbackendutils.controller.advice.UnknownErrorControllerAdvice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandDocumentManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    runApplication<DatalandDocumentManager>(args = args)
}
