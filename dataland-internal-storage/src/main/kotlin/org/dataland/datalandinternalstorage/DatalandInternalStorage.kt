package org.dataland.datalandinternalstorage

import org.dataland.datalandinternalstorage.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandInternalStorage : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland internal storage API process
 */
fun main(args: Array<String>) {
    runApplication<DatalandInternalStorage>(args = args)
}
