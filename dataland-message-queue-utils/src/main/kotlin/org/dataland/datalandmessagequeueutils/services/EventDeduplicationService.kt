package org.dataland.datalandmessagequeueutils.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides idempotent event processing guarantees by tracking processed event IDs.
 * Consumers that need replay-safety call [isDuplicate] before processing and call
 * [markProcessed] after a successful commit.
 *
 * Note: This in-process cache is intentionally kept simple. It prevents duplicate
 * processing within a single service instance lifetime. For cross-instance deduplication,
 * callers should additionally validate entity state (e.g. check current qaStatus before
 * overwriting) as the primary idempotency guard.
 */
@Service
class EventDeduplicationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val processedEventIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Returns true if [eventId] has already been marked processed.
     */
    fun isDuplicate(eventId: String): Boolean = processedEventIds.contains(eventId)

    /**
     * Records [eventId] as processed so subsequent calls to [isDuplicate] return true.
     */
    fun markProcessed(eventId: String) {
        val added = processedEventIds.add(eventId)
        if (!added) {
            logger.debug("Event ID $eventId was already present in the deduplication cache.")
        }
    }

    /**
     * Convenience wrapper: returns true and logs a warning if [eventId] is a duplicate,
     * otherwise marks it processed and returns false.
     */
    fun checkAndMarkProcessed(eventId: String): Boolean {
        if (isDuplicate(eventId)) {
            logger.warn("Duplicate event detected for event ID $eventId – skipping processing.")
            return true
        }
        markProcessed(eventId)
        return false
    }
}
