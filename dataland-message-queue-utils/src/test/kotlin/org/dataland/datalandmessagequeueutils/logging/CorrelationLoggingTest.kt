package org.dataland.datalandmessagequeueutils.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class CorrelationLoggingTest {
    @Test
    fun `adds correlation id to MDC only inside wrapped scope`() {
        assertNull(MDC.get(CorrelationLogging.CORRELATION_ID_KEY))

        CorrelationLogging.withCorrelationId("corr-123") {
            assertEquals("corr-123", MDC.get(CorrelationLogging.CORRELATION_ID_KEY))
        }

        assertNull(MDC.get(CorrelationLogging.CORRELATION_ID_KEY))
    }

    @Test
    fun `restores previous correlation id after wrapped scope`() {
        MDC.put(CorrelationLogging.CORRELATION_ID_KEY, "existing-correlation")

        CorrelationLogging.withCorrelationId("corr-456") {
            assertEquals("corr-456", MDC.get(CorrelationLogging.CORRELATION_ID_KEY))
        }

        assertEquals("existing-correlation", MDC.get(CorrelationLogging.CORRELATION_ID_KEY))
        MDC.remove(CorrelationLogging.CORRELATION_ID_KEY)
    }
}
