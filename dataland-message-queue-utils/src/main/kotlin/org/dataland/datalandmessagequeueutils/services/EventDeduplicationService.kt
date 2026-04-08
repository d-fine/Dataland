package org.dataland.datalandmessagequeueutils.services

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory idempotency helper to suppress duplicate event processing in at-least-once delivery flows.
 */
@Service
class EventDeduplicationService(
    private val retention: Duration = DEFAULT_RETENTION,
    private val nowProvider: () -> Instant = { Instant.now() },
) {
    private val processedEvents = ConcurrentHashMap<String, Instant>()

    /**
     * Checks if an event has already been processed within the retention period.
     *
     * @param eventKey the unique identifier for the event
     * @return true if the event was previously processed, false otherwise
     */
    fun isDuplicate(eventKey: String): Boolean {
        val now = nowProvider()
        pruneExpiredEntries(now)
        val previous = processedEvents.putIfAbsent(eventKey, now)
        return previous != null
    }

    /**
     * Marks an event as processed in the deduplication cache.
     *
     * @param eventKey the unique identifier for the event
     */
    fun markProcessed(eventKey: String) {
        val now = nowProvider()
        pruneExpiredEntries(now)
        processedEvents[eventKey] = now
    }

    private fun pruneExpiredEntries(now: Instant) {
        val threshold = now.minus(retention)
        processedEvents.entries.removeIf { it.value.isBefore(threshold) }
    }

    companion object {
        private val DEFAULT_RETENTION: Duration = Duration.ofHours(6)
    }
}
