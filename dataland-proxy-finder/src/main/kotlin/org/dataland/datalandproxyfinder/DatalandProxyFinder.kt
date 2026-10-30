package org.dataland.datalandproxyfinder

import org.dataland.datalandproxyfinder.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandProxyFinder : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland proxy finder process
 */
fun main(args: Array<String>) {
    runApplication<DatalandProxyFinder>(args = args)
    println("Dataland Proxy Finder started successfully")
}
