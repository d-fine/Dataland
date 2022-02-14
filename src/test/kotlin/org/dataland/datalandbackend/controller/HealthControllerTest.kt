package org.dataland.datalandbackend.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class HealthControllerTest {
    val healthController = HealthController()

    @Test
    fun list_parameters() {
        println(healthController.getHealth().body)
        println(healthController.getHealth().statusCode)
        println(healthController.getHealth().statusCodeValue)
        println(healthController.getHealth().headers)
    }

    @Test
    fun check_healthiness() {
        assertEquals("Healthy", healthController.getHealth().body)
    }
}
