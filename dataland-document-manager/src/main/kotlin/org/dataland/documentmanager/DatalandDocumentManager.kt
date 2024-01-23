package org.dataland.documentmanager

import org.dataland.datalandbackendutils.email.EmailSender
import org.dataland.documentmanager.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(
    basePackages = ["org.dataland"],
    excludeFilters = [Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [EmailSender::class])],
)
class DatalandDocumentManager : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    runApplication<DatalandDocumentManager>(args = args)
}
