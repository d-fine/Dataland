package org.dataland.documentmanager.services.conversion

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Validates the file content of an xlsx document
 */
@Component
class XlsxToXlsxConverter :
    FileConverter(
        allowedMimeTypesPerFileExtension =
            mapOf(
                "xlsx" to
                    setOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/x-tika-ooxml",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        validateNoMacros(file, correlationId)
        validateFileNotEmpty(file, correlationId)
    }

    private fun validateNoMacros(
        document: MultipartFile,
        correlationId: String,
    ) {
        logger.info("Validating that excel file has no macros. (correlation ID: $correlationId)")
        val workbook =
            document.inputStream.use { inputStream ->
                WorkbookFactory.create(inputStream)
            }
        if (workbook is XSSFWorkbook && workbook.isMacroEnabled) {
            throw InvalidInputApiException(
                "No macros allowed.",
                "The Excel file you provided seems to have macros enabled, which is recognized as a " +
                    "potential security issue.",
            )
        }
    }

    private fun validateFileNotEmpty(
        file: MultipartFile,
        correlationId: String,
    ) {
        logger.info("Validating that excel file is not empty. (correlation ID: $correlationId)")
        file.inputStream.use { inputStream ->
            XSSFWorkbook(inputStream).use { workbook ->
                validateWorkbookNotEmpty(workbook)
            }
        }
    }

    private fun validateWorkbookNotEmpty(workbook: XSSFWorkbook) {
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
