package org.dataland.datalandmessagequeueutils.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class EventDeduplicationServiceTest {
    @Test
    fun `marks first-seen event as non-duplicate and second-seen event as duplicate`() {
        val service = EventDeduplicationService()

        assertFalse(service.isDuplicate("event-1"))
        assertTrue(service.isDuplicate("event-1"))
    }

    @Test
    fun `expires old event keys and accepts them again after retention`() {
        var now = Instant.parse("2026-04-08T00:00:00Z")
        val service = EventDeduplicationService(retention = Duration.ofSeconds(1), nowProvider = { now })

        assertFalse(service.isDuplicate("event-2"))
        assertTrue(service.isDuplicate("event-2"))

        now = now.plusSeconds(2)

        assertFalse(service.isDuplicate("event-2"))
    }
}
