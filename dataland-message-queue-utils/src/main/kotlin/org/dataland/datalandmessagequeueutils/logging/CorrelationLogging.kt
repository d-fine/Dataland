package org.dataland.datalandmessagequeueutils.logging

import org.slf4j.MDC

/**
 * Helpers for appending structured correlation context to SLF4J MDC so that all log
 * records emitted during a non-sourceability lifecycle event carry a common identifier.
 */
object CorrelationLogging {
    const val MDC_KEY_CORRELATION_ID = "correlationId"
    const val MDC_KEY_NON_SOURCEABILITY_ID = "nonSourceabilityId"

    /**
     * Executes [block] with [correlationId] bound to MDC under [MDC_KEY_CORRELATION_ID].
     * The MDC entry is removed after the block returns (or throws).
     */
    fun <T> withCorrelationId(
        correlationId: String,
        block: () -> T,
    ): T {
        val previous = MDC.get(MDC_KEY_CORRELATION_ID)
        MDC.put(MDC_KEY_CORRELATION_ID, correlationId)
        return try {
            block()
        } finally {
            if (previous != null) {
                MDC.put(MDC_KEY_CORRELATION_ID, previous)
            } else {
                MDC.remove(MDC_KEY_CORRELATION_ID)
            }
        }
    }

    /**
     * Executes [block] with both [correlationId] and [nonSourceabilityId] bound to MDC.
     * Both entries are cleaned up after the block returns (or throws).
     */
    fun <T> withNonSourceabilityContext(
        correlationId: String,
        nonSourceabilityId: String,
        block: () -> T,
    ): T {
        val previousNsId = MDC.get(MDC_KEY_NON_SOURCEABILITY_ID)
        MDC.put(MDC_KEY_NON_SOURCEABILITY_ID, nonSourceabilityId)
        return try {
            withCorrelationId(correlationId, block)
        } finally {
            if (previousNsId != null) {
                MDC.put(MDC_KEY_NON_SOURCEABILITY_ID, previousNsId)
            } else {
                MDC.remove(MDC_KEY_NON_SOURCEABILITY_ID)
            }
        }
    }
}
