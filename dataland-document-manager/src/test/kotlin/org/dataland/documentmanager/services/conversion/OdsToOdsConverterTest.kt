package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class OdsToOdsConverterTest {
    private val correlationId = "test-correlation-id"
    private val testOds = "sampleFiles/sample.ods"
    private val odsToOdsConverter = OdsToOdsConverter()

    @Test
    fun `verify that a ods file can be converted to ods`() {
        val testInput = MockMultipartFile(
            "test.ods",
            "test.ods",
            "application/vnd.oasis.opendocument.spreadsheet",
            TestUtils().loadFileBytes(testOds),
        )
        assertEquals("application/vnd.oasis.opendocument.spreadsheet", Tika().detect(testInput.bytes))
        val convertedDocument = odsToOdsConverter.convertFile(testInput, correlationId)
        assertEquals("application/vnd.oasis.opendocument.spreadsheet", Tika().detect(convertedDocument))
    }
}
