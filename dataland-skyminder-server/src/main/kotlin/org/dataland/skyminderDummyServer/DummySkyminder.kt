package org.dataland.skyminderDummyServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DummySkyminder

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<DummySkyminder>(*args)
}
