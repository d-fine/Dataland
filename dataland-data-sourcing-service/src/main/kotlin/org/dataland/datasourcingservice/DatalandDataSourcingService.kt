package org.dataland.datasourcingservice

import org.dataland.datasourcingservice.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandDataSourcingService : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland data sourcing process
 */
fun main(args: Array<String>) {
    runApplication<DatalandDataSourcingService>(args = args)
}
