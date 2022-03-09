package org.dataland.skyminderDummyServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class for running the skyminder dummy server
 */
@SpringBootApplication
class DummySkyminder

/**
 * Main function to execute the spring boot service
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<DummySkyminder>(*args)
}
