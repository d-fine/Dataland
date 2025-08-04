package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.utils.DataExportUtils
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
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
        private val specificationApi: SpecificationControllerApi,
    ) {
        companion object {
            private const val HEADER_ROW_INDEX = 0
            private const val COMPANY_NAME_POSITION = -3
            private const val COMPANY_LEI_POSITION = -2
            private const val REPORTING_PERIOD_POSITION = -1
        }

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
            includeAliases: Boolean,
        ): InputStreamResource {
            val jsonData = portfolioData.map { convertDataToJson(it) }
            if (jsonData.isEmpty()) {
                throw DownloadDataNotFoundApiException()
            }

            return when (exportFileType) {
                ExportFileType.CSV ->
                    buildCsvStreamFromPortfolioAsJsonData(
                        jsonData,
                        dataType,
                        keepValueFieldsOnly,
                        includeAliases,
                    )

                ExportFileType.EXCEL ->
                    buildExcelStreamFromPortfolioAsJsonData(
                        jsonData,
                        dataType,
                        keepValueFieldsOnly,
                        includeAliases,
                    )

                ExportFileType.JSON -> buildJsonStreamFromPortfolioAsJsonData(jsonData)
            }
        }

        /**
         * Transform the data to an Excel file with human-readable headers.
         * @param csvDataWithReadableHeaders the data to be transformed (each entry in the list represents a row in the Excel file)
         * @param outputStream the output stream to write the data to
         */
        fun transformDataToExcelWithReadableHeaders(
            csvDataWithReadableHeaders: List<Map<String, String?>>,
            csvSchema: CsvSchema,
            outputStream: OutputStream,
        ) {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Data")

            // Step 1: Get the ordered columns from csvSchema
            val orderedColumns = csvSchema.columnNames // Assume `columnNames` provides the ordered list of columns

            // Step 2: Write the header row
            val headerRow = sheet.createRow(HEADER_ROW_INDEX)
            orderedColumns.forEachIndexed { colIndex, columnName ->
                headerRow.createCell(colIndex).setCellValue(columnName)
            }

            // Step 3: Write the data rows
            csvDataWithReadableHeaders.forEachIndexed { rowIndex, dataMap ->
                val row = sheet.createRow(rowIndex + 1) // Start writing from the second row (index 1)
                orderedColumns.forEachIndexed { colIndex, columnName ->
                    val cellValue = dataMap[columnName] ?: ""
                    row.createCell(colIndex).setCellValue(cellValue)
                }
            }
            orderedColumns.forEachIndexed { index, _ ->
                sheet.autoSizeColumn(index)
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

        private fun getResolvedSchemaNode(framework: String): JsonNode? {
            val resolvedSchemaDto = specificationApi.getResolvedFrameworkSpecification(framework)
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
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isAssembledDatasetParam = (frameworkTemplate != null)

            val (csvData, nonEmptyHeaderFields) =
                DataExportUtils.Companion.getCsvDataAndNonEmptyFields(
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
                DataExportUtils.Companion.getOrderedHeaders(
                    nonEmptyHeaderFields,
                    orderedHeaderFields,
                    isAssembledDatasetParam,
                )
            val readableHeaders =
                if (includeAliases) {
                    DataExportUtils.Companion.applyAliasRenaming(
                        orderedHeaders,
                        frameworkTemplate ?: portfolioExportRows.first(),
                    )
                } else {
                    orderedHeaders.associateWith { it }
                }

            val mappedCsvData = DataExportUtils.Companion.mapReadableHeadersToCsvData(csvData, readableHeaders)

            val usedReadableHeaders =
                mappedCsvData
                    .flatMap { it.entries }
                    .filterNot { it.value.isNullOrBlank() }
                    .map { it.key }
                    .toSet()

            val filteredReadableHeaders = readableHeaders.filterValues { it in usedReadableHeaders }

            val csvSchema =
                DataExportUtils.Companion.createCsvSchemaBuilder(
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
