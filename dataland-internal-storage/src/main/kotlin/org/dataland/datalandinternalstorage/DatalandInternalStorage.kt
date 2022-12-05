package org.dataland.datalandinternalstorage

import org.dataland.datalandinternalstorage.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
class DatalandInternalStorage : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland API key manager process
 */
fun main(args: Array<String>) {
    runApplication<DatalandInternalStorage>(args = args)
}
