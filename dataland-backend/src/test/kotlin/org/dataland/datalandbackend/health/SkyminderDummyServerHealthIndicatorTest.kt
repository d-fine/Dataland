package org.dataland.datalandbackend.health

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SkyminderDummyServerHealthIndicatorTest {
    val skyminderDummyServerHealthIndicator = SkyminderDummyServerHealthIndicator()

    @Test
    fun `test if health of the Skyminder Dummy Server has the status DOWN because it is not responding`() {
        val status: String = skyminderDummyServerHealthIndicator.health().status.code
        assertEquals(
            status, "DOWN",
            "The Skyminder Dummy Server Health Indicator is not returning the status \"DOWN\"." +
                " It should return \"DOWN\", since this is a unit test and no services should be reachable."
        )
    }

    @Test
    fun `test if the Skyminder Dummy Server container is not responding and therefore the output is false`() {
        val isRunningSkyminderServer: Boolean = skyminderDummyServerHealthIndicator.isRunningSkyminderServer()
        assertFalse(
            isRunningSkyminderServer,
            "The Skyminder Dummy Server Health Indicator is not returning the status \"DOWN\"." +
                " It should return \"DOWN\", since this is a unit test and no services should be reachable."
        )
    }
}
