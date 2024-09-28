package org.dataland.documentmanager.services.conversion

import org.apache.poi.hwpf.HWPFDocument
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Converts a doc file to a pdf file
 */
@Component
class DocToPdfConverter(
    @Value("\${dataland.libreoffice.path}")
    pathToLibre: String,
) : MsOfficeToPdfConverterBase(
        converterSourceType = DefaultDocumentFormatRegistry.DOC,
        pathToLibre = pathToLibre,
        allowedMimeTypesPerFileExtension =
            mapOf(
                "doc" to
                    setOf(
                        "application/msword",
                        "application/x-tika-msoffice",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        file.inputStream.use { inputStream ->
            HWPFDocument(inputStream).use { document ->
                validateDocumentContent(document)
            }
        }
    }

    private fun validateDocumentContent(document: HWPFDocument) {
        if (document.range.text().isBlank()) {
            throw InvalidInputApiException(
                fileIsEmptySummary,
                fileIsEmptyMessage,
            )
        }
    }
}
