package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackend.utils.DataExportUtils
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.springframework.core.io.InputStreamResource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Builds export streams (CSV, Excel, JSON) from portfolio export data. Extracted from [DataExportService] to keep
 * that class focused on export job orchestration.
 */
open class ExportStreamBuilder(
    private val datasetAssembler: DatasetAssembler,
    private val specificationService: SpecificationService,
) {
    companion object {
        private const val HEADER_ROW_INDEX = 0
        private const val COMPANY_NAME_POSITION = -3
        private const val COMPANY_LEI_POSITION = -2
        private const val REPORTING_PERIOD_POSITION = -1
        private const val FIXED_COLUMN_WIDTH = 30
        private const val BUFFER = 15
        private const val CONVERT_CHARACTER_WIDTH_TO_EXCEL_UNITS = 256
        private const val EXCEL_FONT_HEIGHT: Short = 11
        private const val MAX_CHARACTER_LENGTH = 255
        private const val MAX_EXCEL_CELL_LENGTH = 32767
    }

    private val objectMapper = defaultObjectMapper

    /**
     * Create a ByteStream to be used for export from a list of SingleCompanyExportData.
     *
     * Note that swagger only supports InputStreamResources and not OutputStreams
     */
    internal fun <T> buildStreamFromPortfolioExportData(
        portfolioData: Collection<SingleCompanyExportData<T>>,
        exportOptions: ExportOptions,
    ): InputStreamResource {
        val jsonData = portfolioData.map { convertDataToJson(it) }
        if (jsonData.isEmpty()) {
            throw DownloadDataNotFoundApiException()
        }
        return when (exportOptions.exportFileType) {
            ExportFileType.CSV -> {
                buildCsvStreamFromPortfolioAsJsonData(
                    jsonData,
                    exportOptions.dataType,
                    exportOptions.keepValueFieldsOnly,
                    exportOptions.includeAliases,
                )
            }

            ExportFileType.EXCEL -> {
                buildExcelStreamFromPortfolioAsJsonData(
                    jsonData,
                    exportOptions.dataType,
                    exportOptions.keepValueFieldsOnly,
                    exportOptions.includeAliases,
                )
            }

            ExportFileType.JSON -> {
                buildJsonStreamFromPortfolioAsJsonData(jsonData)
            }
        }
    }

    /**
     * Transform the data to an Excel file with human-readable headers.
     * @param csvDataWithReadableHeaders the data to be transformed (each entry in the list represents a row in the Excel file)
     * @param csvSchema the CSV schema defining the column structure
     * @param outputStream the output stream to write the data to
     * @param shortHeaderNamesAndColumns if true, column widths are calculated based on character length of the header
     */
    fun transformDataToExcelWithReadableHeaders(
        csvDataWithReadableHeaders: List<Map<String, String?>>,
        csvSchema: CsvSchema,
        outputStream: OutputStream,
        shortHeaderNamesAndColumns: Boolean = false,
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Data")

        val defaultStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.fontName = "Arial"
        font.fontHeightInPoints = EXCEL_FONT_HEIGHT
        defaultStyle.setFont(font)

        // Step 1: Get the ordered columns from csvSchema
        val orderedColumns = csvSchema.columnNames // Assume `columnNames` provides the ordered list of columns

        // Step 2: Write the header row
        val columnHeaderLengths = mutableMapOf<Int, Int>()
        val headerRow = sheet.createRow(HEADER_ROW_INDEX)
        orderedColumns.forEachIndexed { colIndex, columnName ->
            headerRow.createCell(colIndex).apply {
                setCellValue(columnName)
                cellStyle = defaultStyle
                columnHeaderLengths[colIndex] = columnName.length
            }
        }

        // Step 3: Write the data rows and track max column widths
        csvDataWithReadableHeaders.forEachIndexed { rowIndex, dataMap ->
            val row = sheet.createRow(rowIndex + 1) // Start writing from the second row (index 1)
            orderedColumns.forEachIndexed { colIndex, columnName ->
                val cellValue = (dataMap[columnName] ?: "").take(MAX_EXCEL_CELL_LENGTH)
                row.createCell(colIndex).apply {
                    setCellValue(cellValue)
                    cellStyle = defaultStyle
                }
            }
        }

        // Step 4: Set column widths based on configuration
        if (shortHeaderNamesAndColumns) {
            orderedColumns.forEachIndexed { index, _ ->
                val headerLength = columnHeaderLengths[index] ?: FIXED_COLUMN_WIDTH
                val columnWidth =
                    minOf(headerLength + BUFFER, MAX_CHARACTER_LENGTH) * CONVERT_CHARACTER_WIDTH_TO_EXCEL_UNITS
                sheet.setColumnWidth(index, columnWidth)
            }
        } else {
            val columnWidth = (FIXED_COLUMN_WIDTH + BUFFER) * CONVERT_CHARACTER_WIDTH_TO_EXCEL_UNITS
            orderedColumns.forEachIndexed { index, _ ->
                sheet.setColumnWidth(index, columnWidth)
            }
        }

        workbook.write(outputStream)
        workbook.close()
    }

    /**
     * Create a ByteStream to be used for CSV export from a list of JSON objects.
     * @param portfolioExportRows passed JSON objects to be exported
     * @param dataType the datatype specifying the framework
     * @param keepValueFieldsOnly if true, non value fields are stripped
     * @return InputStreamResource byteStream for export.
     * Note that swagger only supports InputStreamResources and not OutputStreams
     */
    private fun buildCsvStreamFromPortfolioAsJsonData(
        portfolioExportRows: List<JsonNode>,
        dataType: DataType,
        keepValueFieldsOnly: Boolean,
        includeAliases: Boolean,
    ): InputStreamResource {
        val (aliasedCsvData, csvSchema) =
            prepareExportData(
                portfolioExportRows, dataType,
                keepValueFieldsOnly, includeAliases,
            )

        val outputStream = ByteArrayOutputStream()
        val csvMapper = CsvMapper()

        csvMapper.writer(csvSchema).writeValues(outputStream).writeAll(aliasedCsvData)
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

    /**
     * Create a ByteStream to be used for Excel export from a list of JSON objects.
     * @param portfolioExportRows passed JSON objects to be exported
     * @param dataType the datatype specifying the framework
     * @param keepValueFieldsOnly if true, non value fields are stripped
     * @param includeAliases if true, human-readable names are used if available
     * @return InputStreamResource byteStream for export.
     * Note that swagger only supports InputStreamResources and not OutputStreams
     */
    private fun buildExcelStreamFromPortfolioAsJsonData(
        portfolioExportRows: List<JsonNode>,
        dataType: DataType,
        keepValueFieldsOnly: Boolean,
        includeAliases: Boolean,
    ): InputStreamResource {
        val (csvData, csvSchema) =
            prepareExportData(
                portfolioExportRows, dataType,
                keepValueFieldsOnly, includeAliases,
            )

        val outputStream = ByteArrayOutputStream()

        transformDataToExcelWithReadableHeaders(
            csvData,
            csvSchema,
            outputStream,
            includeAliases,
        )

        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

    /**
     * Create a ByteStream to be used for JSON Export from a list of JSON objects.
     * @param portfolioExportRows passed data sets to be exported
     * @return InputStreamResource byteStream for export.
     * Note that swagger only supports InputStreamResources and not OutputStreams
     */
    private fun buildJsonStreamFromPortfolioAsJsonData(portfolioExportRows: List<JsonNode>): InputStreamResource {
        val outputStream = ByteArrayOutputStream()

        objectMapper
            .writerFor(List::class.java)
            .writeValue(outputStream, portfolioExportRows)
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

    /**
     * Converts the data class into a JSON object.
     * @param singleCompanyExportData The company associated data
     * @return The JSON node representation of the data
     */
    private fun <T> convertDataToJson(singleCompanyExportData: SingleCompanyExportData<T>): JsonNode {
        val singleCompanyExportDataJson = objectMapper.writeValueAsString(singleCompanyExportData)
        return objectMapper.readTree(singleCompanyExportDataJson)
    }

    private fun getResolvedSchemaNode(framework: String): JsonNode? {
        val resolvedSchemaDto = specificationService.getResolvedFrameworkSpecification(framework)
        return objectMapper.valueToTree(resolvedSchemaDto.resolvedSchema)
    }

    /**
     * Prepares the data structure for export formats (CSV and Excel)
     * @param portfolioExportRows passed JSON objects to be exported
     * @param dataType the datatype specifying the framework
     * @param keepValueFieldsOnly if true, non value fields are stripped
     * @param includeAliases if true, human-readable names are used if available
     * @return PreparedExportData containing:
     *   - the CSV data as a list of maps
     *   - the CSV schema
     *   - header fields with human-readable names
     */
    fun prepareExportData(
        portfolioExportRows: List<JsonNode>,
        dataType: DataType,
        keepValueFieldsOnly: Boolean,
        includeAliases: Boolean,
    ): DataExportUtils.Companion.PreparedExportData {
        val isAssembledDatasetParam = specificationService.isAssembledFramework(dataType.toString())
        val frameworkTemplate = if (isAssembledDatasetParam) datasetAssembler.getFrameworkTemplate(dataType.toString()) else null

        val (csvData, nonEmptyHeaderFields) =
            DataExportUtils.getCsvDataAndNonEmptyFields(
                portfolioExportRows,
                keepValueFieldsOnly,
            )

        val orderedHeaderFields =
            if (isAssembledDatasetParam) {
                val resolvedSchemaNode = getResolvedSchemaNode(dataType.toString())
                JsonUtils.getLeafNodeFieldNames(
                    resolvedSchemaNode ?: NullNode.instance,
                    keepEmptyFields = true,
                    dropLastFieldName = false,
                )
            } else {
                getOrderedHeaderFieldsForNonAssembledDataset(nonEmptyHeaderFields)
            }
        val orderedHeaders =
            DataExportUtils.getOrderedHeaders(
                nonEmptyHeaderFields,
                orderedHeaderFields,
                isAssembledDatasetParam,
            )
        val readableHeaders =
            if (includeAliases) {
                DataExportUtils.applyAliasRenaming(
                    orderedHeaders,
                    frameworkTemplate ?: portfolioExportRows.first(),
                )
            } else {
                orderedHeaders.associateWith { it }
            }

        val mappedCsvData = DataExportUtils.mapReadableHeadersToCsvData(csvData, readableHeaders)

        val usedReadableHeaders =
            mappedCsvData
                .flatMap { it.entries }
                .filterNot { it.value.isNullOrBlank() }
                .map { it.key }
                .toSet()

        val filteredReadableHeaders = readableHeaders.filterValues { it in usedReadableHeaders }

        val csvSchema =
            DataExportUtils.createCsvSchemaBuilder(
                filteredReadableHeaders.values.toSet(),
                orderedHeaders.mapNotNull { filteredReadableHeaders[it] },
                isAssembledDatasetParam,
            )

        return DataExportUtils.Companion.PreparedExportData(mappedCsvData, csvSchema)
    }

    private fun getOrderedHeaderFieldsForNonAssembledDataset(nonEmptyHeaderFields: Set<String>): LinkedHashSet<String> =
        LinkedHashSet(
            nonEmptyHeaderFields.sortedWith(
                compareBy<String> {
                    when {
                        it.startsWith("companyName") -> COMPANY_NAME_POSITION
                        it.startsWith("companyLei") -> COMPANY_LEI_POSITION
                        it.startsWith("reportingPeriod") -> REPORTING_PERIOD_POSITION
                        else -> 0
                    }
                }.then(naturalOrder()),
            ),
        )
}
