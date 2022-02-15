package org.dataland.datalandbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatalandBackend

fun main(args: Array<String>) {
    runApplication<DatalandBackend>(*args)
}
