package org.dataland.documentmanager.services.conversion

import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.jodconverter.core.office.OfficeManager
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

/**
 * Converts a doc/docx file to a pdf file
 */
@Component
class DocxToPdfConverter : FileConverter(
    allowedMimeTypesPerFileExtension = mapOf(
        "docx" to docxMimeTypes,
        "doc" to docxMimeTypes,
    ),
) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val pathToLibre = "/usr/lib/libreoffice"

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        logger.info("Converting doc/docx to a pdf document. (correlation ID: $correlationId)")
        val outputStream = ByteArrayOutputStream()

        val officeManager: OfficeManager = LocalOfficeManager.builder()
            .officeHome(pathToLibre)
            .build()
        officeManager.start()

        val converter = LocalConverter.builder()
            .officeManager(officeManager)
            .build()

        converter.convert(file.inputStream)
            .`as`(DefaultDocumentFormatRegistry.DOCX)
            .to(outputStream)
            .`as`(DefaultDocumentFormatRegistry.PDF)
            .execute()

        officeManager.stop()
        return outputStream.toByteArray()
    }
}

private val docxMimeTypes = setOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/msword",
    "application/x-tika-ooxml",
)
