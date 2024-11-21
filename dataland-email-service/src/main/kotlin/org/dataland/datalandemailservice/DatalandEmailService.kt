package org.dataland.datalandemailservice

import org.dataland.datalandemailservice.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandEmailService : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland email service
 */
fun main(args: Array<String>) {
    runApplication<DatalandEmailService>(args = args)
}
