package org.dataland.frameworktoolbox.utils.diagnostic

import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class DiagnosticManagerTest {
    @Test
    fun `Registering two messages with the same id should fail`() {
        val manager = DiagnosticManager()
        manager.info("test-message", "This is a test")
        assertFails {
            manager.info("test-message", "This is a test 2")
        }
    }

    @Test
    fun `Registering two messages with the same id should fail even if they are suppressed`() {
        val manager = DiagnosticManager()
        manager.suppress("test-message")
        manager.info("test-message", "This is a test")
        assertFails {
            manager.info("test-message", "This is a test 2")
        }
    }

    @Test
    fun `Messages that are uselessly suppressed should cause an error`() {
        val manager = DiagnosticManager()
        manager.suppress("not-happening")
        assertFails {
            manager.finalizeDiagnosticStream()
        }
    }

    @Test
    fun `Registering a critical message should throw an error`() {
        val manager = DiagnosticManager()
        assertFails {
            manager.critical("critical-message", "This is bad")
        }
    }

    @Test
    fun `Registering a warning message should throw an error at the end`() {
        val manager = DiagnosticManager()
        manager.warning("warning-message", "This is (potentially) undesired!")
        assertFails {
            manager.finalizeDiagnosticStream()
        }
    }

    @Test
    fun `Registering a warning message should not throw an error if it was suppressed`() {
        val manager = DiagnosticManager()
        manager.suppress("warning-message")
        manager.warning("warning-message", "This is (potentially) undesired!")
        manager.finalizeDiagnosticStream()
    }
}
