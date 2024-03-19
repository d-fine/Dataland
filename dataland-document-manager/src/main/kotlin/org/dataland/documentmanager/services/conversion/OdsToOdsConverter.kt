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
class OdsToOdsConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "ods" to setOf("application/vnd.oasis.opendocument.spreadsheet"),
    )

    override fun validateFileContent(file: MultipartFile, correlationId: String) {
        logger.info("Validating that ods file is not empty. (correlation ID: $correlationId)")
        file.inputStream.use { inputStream ->
            val document = SpreadsheetDocument.loadDocument(inputStream)
            for (sheetIndex in 0 until document.sheetCount) {
                val sheet = document.getSheetByIndex(sheetIndex)
                if (sheet.rowCount > 0) {
                    return
                }
            }
        }
        throw InvalidInputApiException(
            "An empty spreadsheet was provided",
            "An empty spreadsheet was provided",
        )
    }

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        println("converting")
        return file.bytes
    }
}
