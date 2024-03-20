package org.dataland.documentmanager.services.conversion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile

class FileConverterTest {
    @Test
    fun `check that a proper error is thrown if no file extensions are provided`() {
        val exception = assertThrows<IllegalArgumentException> {
            object : FileConverter(emptyMap()) {
                override val logger = LoggerFactory.getLogger("TestLogger")
                override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
            }
        }
        assertEquals("No file extension for conversion is provided.", exception.message)
    }

    @Test
    fun `check that a proper error is thrown if file extensions are not correctly formatted`() {
        val exception = assertThrows<IllegalArgumentException> {
            object : FileConverter(mapOf("PNG" to setOf("mime-type"))) {
                override val logger = LoggerFactory.getLogger("TestLogger")
                override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
            }
        }
        assertEquals("Some file extensions are not lowercase.", exception.message)
    }

    @Test
    fun `check that no error is thrown if a file converter is correctly initialized`() {
        object : FileConverter(mapOf("png" to setOf("mime-type"))) {
            override val logger = LoggerFactory.getLogger("TestLogger")
            override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
        }
    }
}
