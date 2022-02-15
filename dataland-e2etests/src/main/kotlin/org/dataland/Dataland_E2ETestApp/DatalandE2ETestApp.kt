package org.dataland.Dataland_E2ETestApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatalandE2ETestApp

fun main(args: Array<String>) {
    runApplication<DatalandE2ETestApp>(*args)
}