package org.dataland.datalandapikeymanager

import org.dataland.datalandapikeymanager.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
class DatalandApiKeyManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland API key manager process
 */
fun main(args: Array<String>) {
    runApplication<DatalandApiKeyManager>(*args)
}
