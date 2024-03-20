package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.DocumentManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.multipart.MultipartFile
@SpringBootTest(classes = [DocumentManager::class], properties = ["spring.profiles.active=nodb"])
class PdfConverterTest(
    @Autowired val toPdfConverters: List<FileConverter>,
) {
    private val expectedToPdfConverters = listOf<FileConverter>(
        DocxToPdfConverter(),
        ExcelToExcelConverter(),
        ImageToPdfConverter(),
        OdsToOdsConverter(),
        PdfToPdfConverter(),
        PptxToPdfConverter(),
        TextToPdfConverter(),
    )

    @Test
    fun `check if list of converts is complete`() {
        toPdfConverters.forEach {
            assertTrue(it in expectedToPdfConverters)
        }
        expectedToPdfConverters.forEach {
            assertTrue(it in toPdfConverters)
        }
    }

    @Test
    fun `check if an error is thrown if there are file converters with overlapping file extension responsibility`() {
        val exception = assertThrows<IllegalArgumentException> {
            PdfConverter(
                listOf(
                    object : FileConverter(
                        mapOf(
                            "a" to setOf("abc"),
                            "b" to setOf("defg"),
                        ),
                    ) {
                        override val logger = LoggerFactory.getLogger("TestLogger")
                        override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                    },
                    object : FileConverter(
                        mapOf(
                            "b" to setOf("hijk"),
                            "c" to setOf("lmnop"),
                        ),
                    ) {
                        override val logger = LoggerFactory.getLogger("TestLogger")
                        override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                    },
                ),
            )
        }
        assertEquals("There are multiple file converters which target the same file extensions.", exception.message)
    }

    @Test
    fun `check if no error is thrown if the pdf converter is initialized correctly`() {
        PdfConverter(
            listOf(
                object : FileConverter(
                    mapOf(
                        "a" to setOf("abc"),
                        "b" to setOf("defg"),
                    ),
                ) {
                    override val logger = LoggerFactory.getLogger("TestLogger")
                    override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                },
                object : FileConverter(
                    mapOf(
                        "c" to setOf("lmnop"),
                    ),
                ) {
                    override val logger = LoggerFactory.getLogger("TestLogger")
                    override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                },
            ),
        )
    }
}
