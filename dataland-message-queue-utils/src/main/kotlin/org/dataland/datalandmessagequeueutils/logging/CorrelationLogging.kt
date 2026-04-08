package org.dataland.datalandmessagequeueutils.logging

import org.slf4j.MDC

/**
 * MDC helper for wrapping message handling in a correlation-id logging scope.
 */
object CorrelationLogging {
    const val CORRELATION_ID_KEY: String = "correlationId"

    /**
     * Executes a code block within a correlation ID logging scope.
     * Sets the correlation ID in the MDC for the duration of the block execution.
     *
     * @param correlationId the correlation ID to set in the logging context
     * @param block the code block to execute
     * @return the result of executing the block
     */
    fun <T> withCorrelationId(
        correlationId: String?,
        block: () -> T,
    ): T {
        if (correlationId.isNullOrBlank()) {
            return block()
        }

        val previousCorrelationId = MDC.get(CORRELATION_ID_KEY)
        MDC.put(CORRELATION_ID_KEY, correlationId)

        return try {
            block()
        } finally {
            if (previousCorrelationId == null) {
                MDC.remove(CORRELATION_ID_KEY)
            } else {
                MDC.put(CORRELATION_ID_KEY, previousCorrelationId)
            }
        }
    }
}
