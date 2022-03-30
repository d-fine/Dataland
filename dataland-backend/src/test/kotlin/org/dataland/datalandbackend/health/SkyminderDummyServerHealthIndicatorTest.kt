package org.dataland.datalandbackend.health

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SkyminderDummyServerHealthIndicatorTest {
    val healthIndicatorExtensionForDummyServers = HealthIndicatorExtensionForDummyServers("dummy")

    @Test
    fun `test if health has the status DOWN because no actual server is running`() {
        val status: String = healthIndicatorExtensionForDummyServers.health().status.code
        assertEquals(
            status, "DOWN",
            "The health() method is not returning the status \"DOWN\"." +
                " It should return \"DOWN\", since this is a unit test and no services should be reachable."
        )
    }

    @Test
    fun `test if the output is false because no actual server is running`() {
        val isRunningSkyminderServer: Boolean = healthIndicatorExtensionForDummyServers.isServerRunning()
        assertFalse(
            isRunningSkyminderServer,
            "The health() method is not returning the status \"DOWN\"." +
                " It should return \"DOWN\", since this is a unit test and no services should be reachable."
        )
    }
}
