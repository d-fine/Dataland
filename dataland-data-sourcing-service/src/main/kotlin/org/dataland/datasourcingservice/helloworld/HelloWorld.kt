package org.dataland.datasourcingservice.helloworld

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
This is a test function for setting up the new dataland data sourcing service.
 */
@Profile("nodb")
@Component
class HelloWorld : CommandLineRunner {
    override fun run(vararg args: String?) {
        println("Hello World")
    }
}
