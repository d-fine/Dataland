package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertTrue(isOds(testInput.bytes))
        val convertedDocument = odsToOdsConverter.convert(testInput, correlationId)
        assertTrue(
            isOds(convertedDocument),
            "converted document should be a ods document",
        )
        assertTrue(
            TestUtils().isNotEmptyFile(convertedDocument),
            "converted document should not be empty",
        )
    }
    private fun isOds(byteArray: ByteArray): Boolean {
        val odsSignature = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

        return byteArray.size >= odsSignature.size && byteArray.sliceArray(0 until odsSignature.size)
            .contentEquals(odsSignature)
    }
}
