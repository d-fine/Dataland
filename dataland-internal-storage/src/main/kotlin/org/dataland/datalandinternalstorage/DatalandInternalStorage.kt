package org.dataland.datalandinternalstorage

import org.dataland.datalandbackendutils.email.EmailSender
import org.dataland.datalandinternalstorage.configurations.OpenAPIConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@ComponentScan(
    basePackages = ["org.dataland"],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [EmailSender::class])],
)
class DatalandInternalStorage : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland internal storage API process
 */
fun main(args: Array<String>) {
    runApplication<DatalandInternalStorage>(args = args)
}
