package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class OdsToOdsConverterTest {
    private val correlationId = "test-correlation-id"
    private val testOds = "sampleFiles/sample.ods"
    private val emptyOds = "sampleFiles/empty.ods"
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
        odsToOdsConverter.validateFile(testInput, correlationId)
        val convertedDocument = odsToOdsConverter.convertFile(testInput, correlationId)
        assertEquals("application/vnd.oasis.opendocument.spreadsheet", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty ods file is not validated`() {
        val testInput = MockMultipartFile(
            "test.ods",
            "test.ods",
            "application/vnd.oasis.opendocument.spreadsheet",
            TestUtils().loadFileBytes(emptyOds),
        )
        assertEquals("application/vnd.oasis.opendocument.spreadsheet", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            odsToOdsConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
