package org.dataland.datalanduserservice

import org.dataland.datalanduserservice.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
@EnableScheduling
class DatalandUserService : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    runApplication<DatalandUserService>(args = args)
}
