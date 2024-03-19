package org.dataland.documentmanager.services.conversion

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
import org.odftoolkit.odfdom.doc.table.OdfTable
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
        println("@validation")
        file.inputStream.use { inputStream ->
            val document = OdfSpreadsheetDocument.loadDocument(inputStream)
            document.spreadsheetTables.forEach { table ->
                validateTable(table)
            }
        }
        println("done")
        throw InvalidInputApiException(
            "An empty spreadsheet was provided",
            "An empty spreadsheet was provided",
        )
    }

    private fun validateTable(table: OdfTable) {
        println("table ${table.tableName}")
//        println("new sheet with ${table.rowList.filter {
//            println("row filter")
//            println("hello ${it}")
//            it.cellCount > 0}} rows")
//        if(table.rowCount > 0) {
//            println("@return")
//            return
//        }
        val numColumns = table.columnCount
        table.rowList.forEach { row ->
//            val cellRange = table.getCellRangeByPosition(0, row.rowIndex, numColumns-1, row.rowIndex)
            println("row ${row.rowIndex} of ${table.rowCount}")
//            println("with ${row.cellCount} cells")
            for (i in 0 until numColumns) {
                val cell = row.getCellByIndex(i)
                if (cell.stringValue?.isNotBlank() == true) {
                    return
                }
            }
        }
    }

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        println("converting")
        return file.bytes
    }
}
