package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.gleif.Mapping
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Scheduler {
    @Scheduled(fixedDelay = 10000)
    private fun logging() {
        println("Hallo")
        Mapping().mappingTest()
    }
}
