package org.dataland.datalandexternalstorage

import org.dataland.datalandexternalstorage.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandExternalStorage : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland external storage API process
 */
fun main(args: Array<String>) {
    runApplication<DatalandExternalStorage>(args = args)
}
