package org.dataland.frameworktoolbox.template.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DecimalComponentFactoryTest {
    @Test
    fun `Test that empty input returns no bounds`() {
        val input = ""
        val expected = Pair(null, null)
        val actual = DecimalComponentFactory.parseBounds(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Test that valid input with numeric bounds returns valid bounds`() {
        val input = "Allowed Range: [-10, 20]"
        val expected = Pair(-10L, 20L)
        val actual = DecimalComponentFactory.parseBounds(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Test that valid input with infinite bounds returns valid pair`() {
        val input = "Allowed Range: [-INF, 5]"
        val expected = Pair(null, 5L)
        val actual = DecimalComponentFactory.parseBounds(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Test that invalid input format throws IllegalArgumentException`() {
        val input = "[10,20,30]"
        assertThrows<IllegalArgumentException> { DecimalComponentFactory.parseBounds(input) }
    }
}
