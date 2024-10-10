package org.dataland.documentmanager.services.conversion

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.odftoolkit.simple.SpreadsheetDocument
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Validates the content of an ods file
 */
@Component
class OdsToOdsConverter :
    FileConverter(
        allowedMimeTypesPerFileExtension =
            mapOf(
                "ods" to setOf("application/vnd.oasis.opendocument.spreadsheet"),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        logger.info("Validating that ods file is not empty. (correlation ID: $correlationId)")
        file.inputStream.use { inputStream ->
            SpreadsheetDocument.loadDocument(inputStream).use { document ->
                if (validateDocument(document)) {
                    return
                }
            }
        }
        throw InvalidInputApiException(
            fileIsEmptySummary,
            fileIsEmptyMessage,
        )
    }

    private fun validateDocument(document: SpreadsheetDocument): Boolean {
        for (sheetIndex in 0 until document.sheetCount) {
            val sheet = document.getSheetByIndex(sheetIndex)
            if (sheet.rowCount > 0) {
                return true
            }
        }
        return false
    }

    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray = file.bytes
}
