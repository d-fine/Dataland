package org.dataland.documentmanager.services.conversion

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.jodconverter.core.office.OfficeManager
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

/**
 * Converts a docx file to a pdf file
 */
@Component
class DocxToPdfConverter(
    @Value("\${dataland.libreoffice.path}")
    private val pathToLibre: String,
) : MsOfficeToPdfConverterBase(
        converterSourceType = DefaultDocumentFormatRegistry.DOCX,
        pathToLibre = pathToLibre,
        allowedMimeTypesPerFileExtension =
            mapOf(
                "docx" to
                    setOf(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/x-tika-ooxml",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        file.inputStream.use { inputStream ->
            XWPFDocument(inputStream).use { document ->
                validateDocumentContent(document)
            }
        }
    }

    private fun validateDocumentContent(document: XWPFDocument) {
        if (document.paragraphs.all { it.text.isBlank() }) {
            throw InvalidInputApiException(
                fileIsEmptySummary,
                fileIsEmptyMessage,
            )
        }
    }

    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Converting docx to a pdf document. (correlation ID: $correlationId)")
        val outputStream = ByteArrayOutputStream()

        val officeManager: OfficeManager =
            LocalOfficeManager
                .builder()
                .officeHome(pathToLibre)
                .build()
        officeManager.start()

        val converter =
            LocalConverter
                .builder()
                .officeManager(officeManager)
                .build()

        converter
            .convert(file.inputStream)
            .`as`(DefaultDocumentFormatRegistry.DOCX)
            .to(outputStream)
            .`as`(DefaultDocumentFormatRegistry.PDF)
            .execute()

        officeManager.stop()
        return outputStream.toByteArray()
    }
}
