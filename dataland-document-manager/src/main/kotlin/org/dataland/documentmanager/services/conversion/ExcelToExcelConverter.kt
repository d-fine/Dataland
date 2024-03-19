package org.dataland.documentmanager.services.conversion

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Validates the file content of an excel document
 */
@Component
class ExcelToExcelConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "xlsx" to setOf(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/x-tika-ooxml",
        ),
        "xls" to setOf("application/vnd.ms-excel"),
    )

    override fun validateFileContent(file: MultipartFile, correlationId: String) {
        logger.info("Validating that excel file is not empty. (correlation ID: $correlationId)")
        file.inputStream.use { inputStream ->
            XSSFWorkbook(inputStream).use { workbook ->
                validateWorkbook(workbook)
            }
        }
    }

    private fun validateWorkbook(workbook: XSSFWorkbook) {
        for (i in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(i)
            if (sheet.physicalNumberOfRows > 0) {
                return
            }
        }
        throw InvalidInputApiException(
            "An empty spreadsheet was provided",
            "An empty spreadsheet was provided",
        )
    }

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        return file.bytes
    }
}
