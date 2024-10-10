package org.dataland.datalandbackendutils.services.utils

import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EmailUtilsTest {
    @Test
    fun `validate that the email regex correctly matches dots`() {
        assertEquals("test.test@test.com".isEmailAddress(), true)
        assertEquals("test.test@dev.test.com".isEmailAddress(), true)
        assertEquals("test@test.com".isEmailAddress(), true)
        assertEquals("test@testcom".isEmailAddress(), false)
    }

    @Test
    fun `validate that the email regex is not case sensitive`() {
        assertEquals("Test.Test@test.com".isEmailAddress(), true)
        assertEquals("test.test@TesT.CoM".isEmailAddress(), true)
        assertEquals("TEST@TEST.COM".isEmailAddress(), true)
        assertEquals("TEST@TESTCOM".isEmailAddress(), false)
    }

    @Test
    fun `validate that the email regex enforces correct placement of the at symbol`() {
        assertEquals("@test.com".isEmailAddress(), false)
        assertEquals("test@test.com@".isEmailAddress(), false)
    }
}
