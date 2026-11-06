package org.dataland.datalandcompanyservice

import org.dataland.datalandcompanyservice.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandCompanyServiceApplication : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland company service process
 */
fun main(args: Array<String>) {
    runApplication<DatalandCompanyServiceApplication>(args = args)
    println("Dataland Company Service started successfully")
}
