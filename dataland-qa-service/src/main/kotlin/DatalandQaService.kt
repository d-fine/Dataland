package org.dataland.datalandqaservice

import org.dataland.datalandqaservice.configurations.OpenAPIConfiguration
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.dataland"])
@EnableConfigurationProperties(PreApprovalExemptFieldsConfig::class)
class DatalandQaService : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland internal storage API process
 */
fun main(args: Array<String>) {
    runApplication<DatalandQaService>(args = args)
}
