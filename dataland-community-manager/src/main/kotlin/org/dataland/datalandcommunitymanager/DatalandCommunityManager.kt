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

// TODO With Florian maybe:
// TODO Discuss which flyway baseline version to set in GitHub envs (and which value as fallback in docker-compose)
// TODO Discuss if the initial "table creation" flyway scripts are required for this service or not
// TODO => e.g. backend has those, but the baseline version is 2, so it seems to never run ?
// TODO Ask Florian: Why is the logging level of flyway set to debug mode in the internal storage (but not in backend)?
