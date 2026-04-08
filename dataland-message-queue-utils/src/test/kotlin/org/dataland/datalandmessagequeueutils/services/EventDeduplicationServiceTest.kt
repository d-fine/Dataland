package org.dataland.datalandmessagequeueutils.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EventDeduplicationServiceTest {
    private lateinit var service: EventDeduplicationService

    @BeforeEach
    fun setUp() {
        service = EventDeduplicationService()
    }

    @Test
    fun `isDuplicate returns false for unseen event ID`() {
        assertFalse(service.isDuplicate("event-1"))
    }

    @Test
    fun `isDuplicate returns true after markProcessed`() {
        service.markProcessed("event-2")
        assertTrue(service.isDuplicate("event-2"))
    }

    @Test
    fun `checkAndMarkProcessed returns false and marks on first call`() {
        val result = service.checkAndMarkProcessed("event-3")
        assertFalse(result)
        assertTrue(service.isDuplicate("event-3"))
    }

    @Test
    fun `checkAndMarkProcessed returns true on second call with same ID`() {
        service.checkAndMarkProcessed("event-4")
        val secondResult = service.checkAndMarkProcessed("event-4")
        assertTrue(secondResult)
    }

    @Test
    fun `distinct event IDs are tracked independently`() {
        service.markProcessed("event-5")
        assertFalse(service.isDuplicate("event-6"))
    }
}
