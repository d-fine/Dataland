package org.dataland.documentmanager.services.conversion

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Validates the file content of an xlsx document
 */
@Component
class XlsToXlsConverter :
    FileConverter(
        allowedMimeTypesPerFileExtension =
            mapOf(
                "xls" to
                    setOf(
                        "application/vnd.ms-excel",
                        "application/x-tika-msoffice",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        validateFileNotEmpty(file, correlationId)
    }

    private fun validateFileNotEmpty(
        file: MultipartFile,
        correlationId: String,
    ) {
        logger.info("Validating that excel file is not empty. (correlation ID: $correlationId)")
        file.inputStream.use { inputStream ->
            HSSFWorkbook(inputStream).use { workbook ->
                validateWorkbookNotEmpty(workbook)
            }
        }
    }

    private fun validateWorkbookNotEmpty(workbook: HSSFWorkbook) {
        for (i in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(i)
            if (sheet.physicalNumberOfRows > 0) {
                return
            }
        }
        throw InvalidInputApiException(
            fileIsEmptySummary,
            fileIsEmptyMessage,
        )
    }

    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ) = file.bytes
}
