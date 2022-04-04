package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AnnotationProcessorTest {

    @Test
    fun `check if the allowed data types is not empty`() {
        val dataTypes = AnnotationProcessor().getAllDataTypes()
        Assertions.assertFalse(dataTypes.isEmpty())
    }
}
