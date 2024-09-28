package org.dataland.frameworktoolbox.utils.diagnostic

import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class DiagnosticManagerTest {
    private val testMessageId = "test-message"
    private val testMessageSummary = "Test Message Summary"

    @Test
    fun `registering two messages with the same id should fail`() {
        val manager = DiagnosticManager()
        manager.info(testMessageId, testMessageSummary)
        assertFails {
            manager.info(testMessageId, testMessageSummary)
        }
    }

    @Test
    fun `registering two messages with the same id should fail even if they are suppressed`() {
        val manager = DiagnosticManager()
        manager.suppress(testMessageId)
        manager.info(testMessageId, testMessageSummary)
        assertFails {
            manager.info(testMessageId, testMessageSummary)
        }
    }

    @Test
    fun `messages that are uselessly suppressed should cause an error`() {
        val manager = DiagnosticManager()
        manager.suppress("not-happening")
        assertFails {
            manager.finalizeDiagnosticStream()
        }
    }

    @Test
    fun `registering a critical message should throw an error`() {
        val manager = DiagnosticManager()
        assertFails {
            manager.critical(testMessageId, testMessageSummary)
        }
    }

    @Test
    fun `registering a warning message should throw an error at the end`() {
        val manager = DiagnosticManager()
        manager.warning(testMessageId, testMessageSummary)
        assertFails {
            manager.finalizeDiagnosticStream()
        }
    }

    @Test
    fun `registering a warning message should not throw an error if it was suppressed`() {
        val manager = DiagnosticManager()
        manager.suppress(testMessageId)
        manager.warning(testMessageId, testMessageSummary)
        manager.finalizeDiagnosticStream()
    }
}
