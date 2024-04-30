package org.dataland.dummyeurodatclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class to define the spring boot application
 */
@SpringBootApplication
class DummyEurodatClientService

/**
 * Main function to be executed for running the spring boot process
 */
fun main(args: Array<String>) {
    runApplication<DummyEurodatClientService>(args = args)
}
