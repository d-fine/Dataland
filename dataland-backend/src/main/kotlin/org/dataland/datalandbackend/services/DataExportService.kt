package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.slf4j.LoggerFactory
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

        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Create a ByteStream to be used for Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildStreamFromCompanyAssociatedData(
            companyAssociatedData: List<CompanyAssociatedData<T>>,
            exportFileType: ExportFileType,
            dataType: DataType,
            includeDataMetaInformation: Boolean = false,
        ): InputStreamResource {
            val jsonData = companyAssociatedData.map { convertDataToJson(it) }
            return when (exportFileType) {
                ExportFileType.CSV -> buildCsvStreamFromCompanyAssociatedData(jsonData, dataType, false)
                ExportFileType.EXCEL -> buildCsvStreamFromCompanyAssociatedData(jsonData, dataType, true)
                ExportFileType.JSON -> buildJsonStreamFromCompanyAssociatedData(jsonData)
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
         * Create a ByteStream to be used for CSV Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        private fun buildCsvStreamFromCompanyAssociatedData(
            companyAssociatedData: List<JsonNode>,
            dataType: DataType,
            excelCompatibility: Boolean,
        ): InputStreamResource {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isLegobrickFramework = (frameworkTemplate != null)
            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(companyAssociatedData)

            val allHeaderFields =
                if (isLegobrickFramework) {
                    JsonUtils.getLeafNodeFieldNames(
                        getFrameworkTemplate(dataType.toString()) ?: companyAssociatedData.first(),
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
         * Create a ByteStream to be used for JSON Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        private fun buildJsonStreamFromCompanyAssociatedData(companyAssociatedData: List<JsonNode>): InputStreamResource {
            val outputStream = ByteArrayOutputStream()

            objectMapper
                .writerFor(List::class.java)
                .writeValue(outputStream, companyAssociatedData)
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Parse a list of JSON nodes into a list of (fieldName --> fieldValue)-mappings
         *
         * @param nodes the list of nodes to process
         * @return a pair of the list containing (fieldName --> fieldValue)-mappings and a set of all used field names
         */
        private fun getCsvDataAndNonEmptyFields(nodes: List<JsonNode>): Pair<List<Map<String, String>>, Set<String>> {
            val csvData = nodes.map { JsonUtils.getNonEmptyNodesAsMapping(it) }
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
         * @param companyAssociatedData The company associated data
         * @return The JSON node representation of the data
         */
        private fun <T> convertDataToJson(companyAssociatedData: CompanyAssociatedData<T>): JsonNode {
            val companyAssociatedDataJson = objectMapper.writeValueAsString(companyAssociatedData)
            return objectMapper.readTree(companyAssociatedDataJson)
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
