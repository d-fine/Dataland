package org.dataland.datalandcommunitymanager

import org.dataland.datalandcommunitymanager.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
class DatalandCommunityManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    runApplication<DatalandCommunityManager>(args = args)
}

//  TODO Ask Florian: Why is the logging level of flyway set to debug mode in the internal storage (but not in backend)?
