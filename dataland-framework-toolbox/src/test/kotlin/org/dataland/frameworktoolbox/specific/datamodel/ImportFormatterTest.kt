package org.dataland.frameworktoolbox.specific.datamodel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ImportFormatterTest {
    @Test
    fun `short imports should not be split`() {
        val import = "hello.there"
        assertEquals("import $import", ImportFormatter.splitLongImport(import))
    }

    @Test
    fun `long imports should be split`() {
        val import = "a".repeat(110) + ".thisisverylong"
        val expectedOutput = "import " + "a".repeat(110) + "\n    .thisisverylong"
        assertEquals(expectedOutput, ImportFormatter.splitLongImport(import))
    }

    @Test
    fun `long imports should be split across many lines`() {
        val import = "a".repeat(110) + "." + "b".repeat(110) + "." + "c".repeat(110) + "." + "d".repeat(110)
        val expectedOutput = "import " + "a".repeat(110) +
            "\n    ." + "b".repeat(110) +
            "\n    ." + "c".repeat(110) +
            "\n    ." + "d".repeat(110)
        assertEquals(expectedOutput, ImportFormatter.splitLongImport(import))
    }
}
