package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class DocToPdfConverterTest {
    private val testDoc = "sampleFiles/sample.doc"
    private val emptyDoc = "sampleFiles/emptyDoc.doc"
    private val correlationId = "test-correlation-id"
    private val docToPdfConverter = DocToPdfConverter("/usr/lib/libreoffice")

    @Test
    fun `verify that a doc file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.doc",
            "test.doc",
            "application/x-tika-msoffice",
            TestUtils().loadFileBytes(testDoc),
        )
        Assertions.assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        docToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = docToPdfConverter.convertFile(testInput, correlationId)
        Assertions.assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty doc file is not validated`() {
        val testInput = MockMultipartFile(
            "test.doc",
            "test.doc",
            "application/msword",
            TestUtils().loadFileBytes(emptyDoc),
        )
        Assertions.assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            docToPdfConverter.validateFile(testInput, correlationId)
        }
        Assertions.assertEquals("Provided file is empty.", exception.message)
    }
}
