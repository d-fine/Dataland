package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
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
         * @param portfolioData passed list of SingleCompanyExportData to be exported
         * @param exportFileType the file type to be exported
         * @param dataType the datatype specifying the framework
         * @param includeDataMetaInformation if false, non value fields are stripped
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildStreamFromPortfolioExportData(
            portfolioData: List<SingleCompanyExportData<T>>,
            exportFileType: ExportFileType,
            dataType: DataType,
            includeDataMetaInformation: Boolean = false,
        ): InputStreamResource {
            val jsonData = portfolioData.map { convertDataToJson(it) }
            return when (exportFileType) {
                ExportFileType.CSV -> buildCsvStreamFromPortfolioAsJsonData(jsonData, dataType, false, includeDataMetaInformation)
                ExportFileType.EXCEL -> buildCsvStreamFromPortfolioAsJsonData(jsonData, dataType, true, includeDataMetaInformation)
                ExportFileType.JSON -> buildJsonStreamFromPortfolioAsJsonData(jsonData)
            }
        }

        /**
         * Return the template of a legobricks framework or null if the passed name refers to an old style framework
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
         * Create a ByteStream to be used for CSV export from SingleCompanyExportData as JSON objects.
         * @param portfolioExportRows passed SingleCompanyExportData as JSON object to be exported
         * @param dataType the datatype specifying the framework
         * @param excelCompatibility whether a separator indicator should be prependet to the stream resource
         * @param includeDataMetaInformation if false, non value fields are stripped
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        private fun buildCsvStreamFromPortfolioAsJsonData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            excelCompatibility: Boolean,
            includeDataMetaInformation: Boolean,
        ): InputStreamResource {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isLegobrickFramework = (frameworkTemplate != null)
            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(portfolioExportRows, includeDataMetaInformation)

            val allHeaderFields =
                if (isLegobrickFramework) {
                    JsonUtils.getLeafNodeFieldNames(
                        getFrameworkTemplate(dataType.toString()) ?: portfolioExportRows.first(),
                        keepEmptyFields = true,
                        dropLastFieldName = true,
                    )
                } else {
                    LinkedHashSet(nonEmptyHeaderFields.sorted())
                }

            val csvSchema = createCsvSchemaBuilder(nonEmptyHeaderFields, allHeaderFields, isLegobrickFramework)

            val outputStream = ByteArrayOutputStream()
            val csvWriter = CsvMapper().writerFor(List::class.java).with(csvSchema)
            if (excelCompatibility) {
                val csvDataAsString = "sep=,\n" + csvWriter.writeValueAsString(csvData)
                objectMapper.writeValue(outputStream, csvDataAsString)
            } else {
                csvWriter.writeValue(outputStream, csvData)
            }
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Create a ByteStream to be used for JSON Export from a list of JSON objects representin SingleCompanyExportData.
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
         * @param includeDataMetaInformation whether meta information fields should be kept or dropped
         * @return a pair of the list containing (fieldName --> fieldValue)-mappings and a set of all used field names
         */
        private fun getCsvDataAndNonEmptyFields(
            nodes: List<JsonNode>,
            includeDataMetaInformation: Boolean,
        ): Pair<List<Map<String, String>>, Set<String>> {
            val csvData =
                nodes.map { node ->
                    val nonEmptyNodes = JsonUtils.getNonEmptyNodesAsMapping(node)
                    if (includeDataMetaInformation) {
                        nonEmptyNodes
                    } else {
                        nonEmptyNodes.filterNotTo(mutableMapOf()) { isMetaDataField(it.key) }
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
            isLegobrickFramework: Boolean,
        ): CsvSchema {
            require(usedHeaderFields.isNotEmpty()) { "After filtering, CSV data is empty." }

            val csvSchemaBuilder = CsvSchema.builder()

            if (isLegobrickFramework) {
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
    }
