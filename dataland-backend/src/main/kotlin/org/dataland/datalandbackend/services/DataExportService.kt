package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.text.SimpleDateFormat

/**
 * Data export service used for managing the logic behind the dataset export controller
 */
@Service
class DataExportService
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val dataPointUtils: DataPointUtils,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) {
        init {
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        }

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
         * Transform the data to an Excel file.
         * @param headerFields the header fields to be used (has to be consistent with the keys in the data map)
         * @param data the data to be transformed (each entry in the list represents a row in the Excel file)
         * @param outputStream the output stream to write the data to
         */
        fun transformDataToExcel(
            headerFields: List<String>,
            data: List<Map<String, String>>,
            outputStream: OutputStream,
        ) {
            val headerToBeUsed = listOf("companyName", "companyLei", "reportingPeriod") + headerFields.map { "data.$it" }
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Data")
            val headerRow = sheet.createRow(0)
            headerToBeUsed.forEachIndexed { index, headerField ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(headerField)
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

            headerToBeUsed.forEachIndexed { index, headerField ->
                sheet.autoSizeColumn(index)
            }

            workbook.write(outputStream)
            workbook.close()
        }

        /**
         * Prepares data for export - extracts headers and formats data
         * @param portfolioExportRows JSON data to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return Triple of prepared data maps, all header fields in order, and whether this is an assembled dataset
         */
        private fun prepareExportData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): Triple<List<Map<String, String>>, List<String>, Boolean> {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isAssembledDataset = (frameworkTemplate != null)
            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(portfolioExportRows, keepValueFieldsOnly)

            val allHeaderFields =
                if (isAssembledDataset) {
                    JsonUtils
                        .getLeafNodeFieldNames(
                            getFrameworkTemplate(dataType.toString()) ?: portfolioExportRows.first(),
                            keepEmptyFields = true,
                            dropLastFieldName = true,
                        ).toList()
                } else {
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
                    )
                }

            return Triple(csvData, allHeaderFields, isAssembledDataset)
        }

        /**
         * Return true if the provided field name (full path) specifies a meta data field.
         */
        private fun isMetaDataField(field: String): Boolean {
            val separator = JsonUtils.getPathSeparator()
            return field.endsWith(separator + "comment") ||
                field.endsWith(separator + "quality") ||
                field.contains(separator + "dataSource" + separator) ||
                field.contains(separator + "referencedReports" + separator)
        }

        /**
         * Create a ByteStream to be used for CSV export from a list of JSON objects.
         * @param portfolioExportRows passed JSON objects to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return InputStreamResource byteStream for CSV export.
         */
        private fun buildCsvStreamFromPortfolioAsJsonData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): InputStreamResource {
            val (csvData, allHeaderFields, isAssembledDataset) =
                prepareExportData(
                    portfolioExportRows, dataType, keepValueFieldsOnly,
                )

            val nonEmptyHeaderFields = csvData.flatMap { it.keys }.toSet()
            val csvSchema = createCsvSchemaBuilder(nonEmptyHeaderFields, allHeaderFields, isAssembledDataset)

            val outputStream = ByteArrayOutputStream()
            val csvMapper = CsvMapper()
            val csvWriter = csvMapper.writerFor(List::class.java).with(csvSchema)
            val rawCsv = csvWriter.writeValueAsString(csvData)

            // Get original header names and create human-readable versions
            val originalHeaders = csvSchema.columnNames
            val readableHeaders = createHumanReadableFieldNames(originalHeaders)
            val humanHeaderLine = originalHeaders.joinToString(",") { readableHeaders[it] ?: it }

            // Replace only the header line
            val csvWithReadableHeaders =
                rawCsv
                    .lineSequence()
                    .toList()
                    .let {
                        listOf(humanHeaderLine) + it.drop(1)
                    }.joinToString("\n")

            outputStream.write(csvWithReadableHeaders.toByteArray())
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Create a ByteStream to be used for Excel export from a list of JSON objects.
         * @param portfolioExportRows passed JSON objects to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @return InputStreamResource byteStream for Excel export.
         */
        private fun buildExcelStreamFromPortfolioAsJsonData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
        ): InputStreamResource {
            val (csvData, allHeaderFields, _) =
                prepareExportData(
                    portfolioExportRows, dataType, keepValueFieldsOnly,
                )

            val outputStream = ByteArrayOutputStream()
            transformDataToExcel(allHeaderFields, csvData, outputStream)
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
         *
         * @param nodes the list of nodes to process
         * @param keepValueFieldsOnly whether meta-information fields should be dropped or kept
         * @return a pair of lists containing (fieldName --> fieldValue)-mappings and a set of all used field names
         */
        private fun getCsvDataAndNonEmptyFields(
            nodes: List<JsonNode>,
            keepValueFieldsOnly: Boolean,
        ): Pair<List<Map<String, String>>, Set<String>> {
            val csvData =
                nodes.map { node ->
                    val nonEmptyNodes = JsonUtils.getNonEmptyLeafNodesAsMapping(node)
                    if (keepValueFieldsOnly) {
                        nonEmptyNodes.filterNotTo(mutableMapOf()) { isMetaDataField(it.key) }
                    } else {
                        nonEmptyNodes
                    }
                }
            val nonEmptyFields = csvData.map { it.keys }.fold(emptySet<String>()) { acc, next -> acc.plus(next) }

            csvData.forEach { dataSet ->
                nonEmptyFields.forEach { headerField ->
                    dataSet.getOrPut(headerField) { "" }
                }
            }

            return Pair(csvData, nonEmptyFields)
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
         *
         * The first parameter determines which fields are used to create columns; the second parameter determines the
         * order of the columns.
         *
         * @param usedHeaderFields a set of column names used as the headers in the CSV
         * @param allHeaderFields a list of all existing header fields in the correct order
         * @return the csv schema builder
         */
        private fun createCsvSchemaBuilder(
            usedHeaderFields: Set<String>,
            allHeaderFields: Collection<String>,
            isAssembledDataset: Boolean,
        ): CsvSchema {
            require(usedHeaderFields.isNotEmpty()) { "After filtering, CSV data is empty." }

            val csvSchemaBuilder = CsvSchema.builder()

            if (isAssembledDataset) {
                usedHeaderFields
                    .filter {
                        !it.startsWith("data" + JsonUtils.getPathSeparator())
                    }.forEach { csvSchemaBuilder.addColumn(it) }

                allHeaderFields.forEach { allHeaderFieldsEntry ->
                    usedHeaderFields
                        .filter { usedHeaderField ->
                            usedHeaderField.startsWith("data" + JsonUtils.getPathSeparator() + allHeaderFieldsEntry)
                        }.forEach {
                            csvSchemaBuilder.addColumn(it)
                        }
                }
            } else {
                allHeaderFields.forEach {
                    csvSchemaBuilder.addColumn(it)
                }
            }
            return csvSchemaBuilder.build().withHeader()
        }

        private fun createHumanReadableFieldNames(fields: Collection<String>): Map<String, String> =
            fields.associateWith { original ->
                val parts = original.split('.')
                val relevantParts = if (parts.firstOrNull() == "data") parts.drop(1) else parts

                val formattedString =
                    relevantParts.joinToString(" ") { part ->
                        part
                            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                            .replaceFirstChar { it.uppercaseChar() }
                    }

                formattedString.replace("General General", "General")
            }
    }
