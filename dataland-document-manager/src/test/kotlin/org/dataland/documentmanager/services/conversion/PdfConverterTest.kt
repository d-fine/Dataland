package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.DatalandDocumentManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.multipart.MultipartFile
@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
class PdfConverterTest(
    @Autowired val toPdfConverters: List<FileConverter>,
) {
    private val expectedToPdfConverters = listOf(
        DocxToPdfConverter::class.java,
        ExcelToExcelConverter::class.java,
        ImageToPdfConverter::class.java,
        OdsToOdsConverter::class.java,
        PdfToPdfConverter::class.java,
        PptxToPdfConverter::class.java,
        TextToPdfConverter::class.java,
    )

    @Test
    fun `check if list of converts is complete`() {
        toPdfConverters.forEach {
            assertTrue(
                it.javaClass in expectedToPdfConverters,
                "converter ${it.javaClass} is not expected",
            )
        }
        assertEquals(
            expectedToPdfConverters.size, toPdfConverters.size,
            "expected number of pdf converters",
        )
    }

    @Test
    fun `check if an error is thrown if there are file converters with overlapping file extension responsibility`() {
        val exception = assertThrows<IllegalArgumentException> {
            FileProcessor(
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
        FileProcessor(
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
