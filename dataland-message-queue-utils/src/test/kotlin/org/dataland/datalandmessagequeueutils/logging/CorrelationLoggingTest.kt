package org.dataland.datalandmessagequeueutils.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class CorrelationLoggingTest {
    @Test
    fun `withCorrelationId puts correlation ID into MDC during block execution`() {
        var capturedId: String? = null
        CorrelationLogging.withCorrelationId("corr-123") {
            capturedId = MDC.get(CorrelationLogging.MDC_KEY_CORRELATION_ID)
        }
        assertEquals("corr-123", capturedId)
    }

    @Test
    fun `withCorrelationId removes correlation ID from MDC after block`() {
        CorrelationLogging.withCorrelationId("corr-cleanup") { /* no-op */ }
        assertNull(MDC.get(CorrelationLogging.MDC_KEY_CORRELATION_ID))
    }

    @Test
    fun `withCorrelationId restores previous correlation ID after nested call`() {
        MDC.put(CorrelationLogging.MDC_KEY_CORRELATION_ID, "outer")
        CorrelationLogging.withCorrelationId("inner") { /* no-op */ }
        assertEquals("outer", MDC.get(CorrelationLogging.MDC_KEY_CORRELATION_ID))
        MDC.remove(CorrelationLogging.MDC_KEY_CORRELATION_ID)
    }

    @Test
    fun `withNonSourceabilityContext sets both MDC keys during block`() {
        var capturedCorrId: String? = null
        var capturedNsId: String? = null
        CorrelationLogging.withNonSourceabilityContext("corr-abc", "ns-xyz") {
            capturedCorrId = MDC.get(CorrelationLogging.MDC_KEY_CORRELATION_ID)
            capturedNsId = MDC.get(CorrelationLogging.MDC_KEY_NON_SOURCEABILITY_ID)
        }
        assertEquals("corr-abc", capturedCorrId)
        assertEquals("ns-xyz", capturedNsId)
    }

    @Test
    fun `withNonSourceabilityContext removes both MDC keys after block`() {
        CorrelationLogging.withNonSourceabilityContext("corr-rm", "ns-rm") { /* no-op */ }
        assertNull(MDC.get(CorrelationLogging.MDC_KEY_CORRELATION_ID))
        assertNull(MDC.get(CorrelationLogging.MDC_KEY_NON_SOURCEABILITY_ID))
    }
}
