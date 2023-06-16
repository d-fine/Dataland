package org.dataland.datalandbackend

import org.dataland.datalandbackend.configurations.OpenAPIConfiguration
import org.dataland.datalandbackendutils.configurations.DefaultResponseSchemaCustomizer
import org.dataland.datalandbackendutils.configurations.RequestRejectedExceptionHandler
import org.dataland.datalandbackendutils.controller.advice.KnownErrorControllerAdvice
import org.dataland.datalandbackendutils.controller.advice.UnknownErrorControllerAdvice
import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
@Import(
    value = [
        KnownErrorControllerAdvice::class,
        UnknownErrorControllerAdvice::class,
        DefaultResponseSchemaCustomizer::class,
        RequestRejectedExceptionHandler::class,
    ],
)
@ComponentScan(basePackages = ["org.dataland"])
class DatalandBackend : OpenAPIConfiguration

/**
 * Main function to be executed for running the spring boot dataland backend process
 */
fun main(args: Array<String>) {
    val dataSource = org.springframework.jdbc.datasource.DriverManagerDataSource()
    dataSource.url = "jdbc:postgresql://127.0.0.1:5433/backend"
    dataSource.username = "backend"
    dataSource.password = System.getenv("BACKEND_DB_PASSWORD")
    // val springDataSource = org.springframework.jdbc.datasource.init.DataSourceInitializer()
    val flyway = Flyway.configure().dataSource(dataSource).load()
    flyway.baseline()
    flyway.migrate()
    runApplication<DatalandBackend>(args = args)
}
