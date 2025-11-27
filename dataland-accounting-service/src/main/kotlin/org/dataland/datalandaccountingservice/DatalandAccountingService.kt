package org.dataland.datalandaccountingservice

import org.dataland.datalandaccountingservice.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandAccountingService : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland API key manager process
 */
fun main(args: Array<String>) {
    runApplication<DatalandAccountingService>(args = args)
    println("Dataland Accounting Service started successfully")
}
