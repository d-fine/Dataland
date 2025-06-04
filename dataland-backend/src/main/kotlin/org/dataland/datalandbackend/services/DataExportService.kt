package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Data export service used for managing the logic behind the dataset export controller
 */
@Service
class DataExportService
    @Autowired
    constructor(
        private val dataPointUtils: DataPointUtils,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) {
        private val objectMapper = JsonUtils.defaultObjectMapper

        /**
         * Create a ByteStream to be used for export from a list of SingleCompanyExportData.
         * @param portfolioData the passed list of SingleCompanyExportData to be exported
         * @param exportFileType the file type to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildStreamFromPortfolioExportData(
            portfolioData: List<SingleCompanyExportData<T>>,
            exportFileType: ExportFileType,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): InputStreamResource {
            val jsonData = portfolioData.map { convertDataToJson(it) }
            if (jsonData.isEmpty()) {
                throw DownloadDataNotFoundApiException()
            }
            return when (exportFileType) {
                ExportFileType.CSV -> buildCsvStreamFromPortfolioAsJsonData(jsonData, dataType, keepValueFieldsOnly)
                ExportFileType.EXCEL -> buildExcelStreamFromPortfolioAsJsonData(jsonData, dataType, keepValueFieldsOnly)
                ExportFileType.JSON -> buildJsonStreamFromPortfolioAsJsonData(jsonData)
            }
        }

        /**
         * Return the template of an assembled framework or null if the passed name refers to an old style framework
         *
         * @param framework the framework for which the template shall be returned
         */
        private fun getFrameworkTemplate(framework: String): JsonNode? {
            return dataPointUtils.getFrameworkSpecificationOrNull(framework)?.let {
                val frameworkTemplate = objectMapper.readTree(it.schema)
                referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(
                    frameworkTemplate,
                    it.referencedReportJsonPath,
                )
                return frameworkTemplate
            }
        }

        /**
         * Transform the data to an Excel file with human-readable headers.
         * @param headerFields the header fields to be used (has to be consistent with the keys in the data map)
         * @param data the data to be transformed (each entry in the list represents a row in the Excel file)
         * @param outputStream the output stream to write the data to
         * @param readableHeaders a map of original header names to readable header names
         */
        fun transformDataToExcelWithReadableHeaders(
            headerFields: List<String>,
            data: List<Map<String, String>>,
            outputStream: OutputStream,
            readableHeaders: Map<String, String> = emptyMap(),
        ) {
            val headerToBeUsed = listOf("companyName", "companyLei", "reportingPeriod") + headerFields.map { "data.$it" }
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Data")
            val headerRow = sheet.createRow(0)
            headerToBeUsed.forEachIndexed { index, headerField ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(readableHeaders[headerField] ?: headerField)
            }

            var rowIndex = 1
            data.forEach { entry ->
                val row = sheet.createRow(rowIndex++)
                headerToBeUsed.forEachIndexed { index, headerField ->
                    val cell = row.createCell(index)
                    if (headerField in entry.keys) {
                        cell.setCellValue(entry[headerField] ?: "")
                    } else if ("$headerField.value" in entry.keys) {
                        cell.setCellValue(entry["$headerField.value"] ?: "")
                    } else {
                        cell.setCellValue("")
                    }
                }
            }

            headerToBeUsed.forEachIndexed { index, _ ->
                sheet.autoSizeColumn(index)
            }

            workbook.write(outputStream)
            workbook.close()
        }

        /**
         * Return true if the field contains REFERENCED_REPORTS_ID
         */
        private fun isReferencedReportsField(field: String): Boolean =
            field.contains(JsonUtils.getPathSeparator() + ReferencedReportsUtilities.REFERENCED_REPORTS_ID + JsonUtils.getPathSeparator())

        /**
         * Return true if the provided field name (full path) specifies a meta data field.
         */
        private fun isMetaDataField(field: String): Boolean {
            val separator = JsonUtils.getPathSeparator()
            return field.endsWith(separator + "comment") ||
                field.endsWith(separator + "quality") ||
                field.contains(separator + "dataSource" + separator) ||
                isReferencedReportsField(field)
        }

        /**
         * Prepares the data structure for export formats (CSV and Excel)
         * @param portfolioExportRows passed JSON objects to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return PreparedExportData containing:
         *   - the CSV data as a list of maps
         *   - the CSV schema
         *   - header fields with human-readable names
         */
        private fun prepareExportData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): PreparedExportData {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isAssembledDataset = (frameworkTemplate != null)
            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(portfolioExportRows, keepValueFieldsOnly)

            val orderedHeaderFields =
                if (isAssembledDataset) {
                    JsonUtils.getLeafNodeFieldNames(
                        getFrameworkTemplate(dataType.toString()) ?: portfolioExportRows.first(),
                        keepEmptyFields = true,
                        dropLastFieldName = true,
                    )
                } else {
                    LinkedHashSet(
                        nonEmptyHeaderFields.sortedWith(
                            compareBy<String> {
                                @Suppress("MagicNumber")
                                when {
                                    it.startsWith("companyName") -> -3
                                    it.startsWith("companyLei") -> -2
                                    it.startsWith("reportingPeriod") -> -1
                                    else -> 0
                                }
                            }.then(naturalOrder()),
                        ),
                    )
                }
            val csvSchema = createCsvSchemaBuilder(nonEmptyHeaderFields, orderedHeaderFields, isAssembledDataset)
            return PreparedExportData(csvData, csvSchema)
        }

        private data class PreparedExportData(
            val csvData: List<Map<String, String>>,
            val csvSchema: CsvSchema,
        )

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
        ): InputStreamResource {
            val (csvData, csvSchema) =
                prepareExportData(
                    portfolioExportRows,
                    dataType,
                    keepValueFieldsOnly,
                )

            val outputStream = ByteArrayOutputStream()
            val csvMapper = CsvMapper()
            val csvWriter = csvMapper.writerFor(List::class.java).with(csvSchema)
            val rawCsv = csvWriter.writeValueAsString(csvData)

            outputStream.write(rawCsv.toByteArray())
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Create a ByteStream to be used for Excel export from a list of JSON objects.
         * @param portfolioExportRows passed JSON objects to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        private fun buildExcelStreamFromPortfolioAsJsonData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): InputStreamResource {
            val (csvData, csvSchema) =
                prepareExportData(
                    portfolioExportRows,
                    dataType,
                    keepValueFieldsOnly,
                )
            val excelHeaderFields =
                csvSchema.columnNames
                    .filter { it.startsWith("data.") }
                    .map { it.substringAfter("data.") }
            val outputStream = ByteArrayOutputStream()
            transformDataToExcelWithReadableHeaders(excelHeaderFields, csvData, outputStream)
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
         * Parse a list of JSON nodes into a list of (fieldName --> fieldValue)-mappings
         * @param nodes the list of nodes to process
         * @param keepValueFieldsOnly whether meta-information fields should be dropped or kept
         * @return a pair of lists containing (fieldName --> fieldValue)-mappings and a set of all used field names
         */
        private fun getCsvDataAndNonEmptyFields(
            nodes: List<JsonNode>,
            keepValueFieldsOnly: Boolean,
        ): Pair<List<Map<String, String>>, Set<String>> {
            val csvData =
                getCsvData(nodes, keepValueFieldsOnly)
            val nonEmptyFields = csvData.map { it.keys }.fold(emptySet<String>()) { acc, next -> acc.plus(next) }

            csvData.forEach { dataSet ->
                nonEmptyFields.forEach { headerField ->
                    dataSet.getOrPut(headerField) { "" }
                }
            }

            return Pair(csvData, nonEmptyFields)
        }

        private fun getCsvData(
            nodes: List<JsonNode>,
            keepValueFieldsOnly: Boolean,
        ): List<MutableMap<String, String>> =
            nodes.map { node ->
                val nonEmptyNodes =
                    JsonUtils
                        .getNonEmptyLeafNodesAsMapping(node)
                        .filterKeys { !isReferencedReportsField(it) }
                        .toMutableMap()

                if (keepValueFieldsOnly) {
                    processQualityFields(nonEmptyNodes)
                } else {
                    nonEmptyNodes
                }
            }

        /**
         * Process a map of nodes to keep value fields and convert quality fields to value fields
         * when there is no corresponding value.
         * @param nodes The map of nodes to process
         * @return A filtered map containing only value fields (including converted quality fields)
         */
        private fun processQualityFields(nodes: MutableMap<String, String>): MutableMap<String, String> {
            val filteredNodes = mutableMapOf<String, String>()
            val separator = JsonUtils.getPathSeparator()

            nodes.keys.toList().forEach { field ->
                if (field.endsWith("${separator}quality")) {
                    val basePath = field.substringBeforeLast("${separator}quality")
                    val valuePath = "${basePath}${separator}value"
                    if (valuePath !in nodes) {
                        nodes[field]?.let { qualityValue ->
                            filteredNodes[valuePath] = qualityValue
                        }
                    }
                    return@forEach // go to next iteration
                }

                if (field.endsWith("${separator}value")) {
                    nodes[field]?.let { value ->
                        filteredNodes[field] = value
                    }
                    return@forEach
                }

                if (!isMetaDataField(field)) {
                    nodes[field]?.let { value ->
                        filteredNodes[field] = value
                    }
                }
            }

            nodes.clear()
            nodes.putAll(filteredNodes)
            return nodes
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

        /**
         * Creates the CSV schema based on the provided headers
         * The first parameter determines which fields are used to create columns; the second parameter determines the
         * order of the columns.
         * @param usedHeaderFields a set of column names used as the headers in the CSV
         * @param orderedHeaderFields a list of all existing header fields in the correct order
         * @return the csv schema builder
         */
        private fun createCsvSchemaBuilder(
            usedHeaderFields: Set<String>,
            orderedHeaderFields: Collection<String>,
            isAssembledDataset: Boolean,
        ): CsvSchema {
            require(usedHeaderFields.isNotEmpty()) { "After filtering, CSV data is empty." }

            val csvSchemaBuilder = CsvSchema.builder()

            if (isAssembledDataset) {
                usedHeaderFields
                    .filter {
                        !it.startsWith("data" + JsonUtils.getPathSeparator())
                    }.forEach { csvSchemaBuilder.addColumn(it) }

                orderedHeaderFields.forEach { orderedHeaderFieldsEntry ->
                    usedHeaderFields
                        .filter { usedHeaderField ->
                            usedHeaderField.startsWith("data" + JsonUtils.getPathSeparator() + orderedHeaderFieldsEntry)
                        }.forEach {
                            csvSchemaBuilder.addColumn(it)
                        }
                }
            } else {
                orderedHeaderFields.forEach {
                    csvSchemaBuilder.addColumn(it)
                }
            }
            return csvSchemaBuilder.build().withHeader()
        }
    }
