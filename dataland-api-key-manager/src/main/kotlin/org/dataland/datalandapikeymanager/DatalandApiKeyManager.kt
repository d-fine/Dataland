package org.dataland.datalandapikeymanager

import org.dataland.datalandapikeymanager.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandApiKeyManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland API key manager process
 */
fun main(args: Array<String>) {
    runApplication<DatalandApiKeyManager>(args = args)
}
