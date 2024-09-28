package org.dataland.frameworktoolbox

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FrameworkToolboxCliTest {
    @Test
    fun `an error message should be thrown if called without arguments`() {
        assertThrows<IllegalArgumentException> {
            FrameworkToolboxCli().invoke(emptyArray())
        }
    }

    @Test
    fun `an error message should be thrown if more arguments are specified than supported`() {
        assertThrows<IllegalArgumentException> {
            FrameworkToolboxCli().invoke(arrayOf("all", "unsupported"))
        }
        assertThrows<IllegalArgumentException> {
            FrameworkToolboxCli().invoke(arrayOf("lksg", "unsupported"))
        }
        assertThrows<IllegalArgumentException> {
            FrameworkToolboxCli().invoke(arrayOf("list", "file", "unsupported"))
        }
    }

    @Test
    fun `an error should be thrown when a build is requested for an unknown framework`() {
        assertThrows<IllegalArgumentException> {
            FrameworkToolboxCli().invoke(arrayOf("unknown-framework"))
        }
    }
}
