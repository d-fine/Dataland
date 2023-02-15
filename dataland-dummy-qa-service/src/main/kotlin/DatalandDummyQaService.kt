package org.dataland.datalanddummyqaservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
class DatalandDummyQaService

/**
 * Main function to be executed for running the spring boot dataland internal storage API process
 */
fun main(args: Array<String>) {
    runApplication<DatalandDummyQaService>(args = args)
}
