package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
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
    ) {
        init {
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        }

        /**
         * Create a ByteStream to be used for CSV Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildCsvStreamFromCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>): InputStreamResource {
            val jsonTree: JsonNode = convertDataToJson(companyAssociatedData)

            val csvSchemaAndData = createCsvSchemaAndDataFromJson(jsonTree)
            val csvSchema = csvSchemaAndData.first
            val csvData = csvSchemaAndData.second
            val outputStream = ByteArrayOutputStream()

            CsvMapper()
                .writerFor(Map::class.java)
                .with(csvSchema)
                .writeValue(outputStream, csvData)
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Create a ByteStream to be used for Excel Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildExcelStreamFromCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>): InputStreamResource {
            val jsonTree: JsonNode = convertDataToJson(companyAssociatedData)

            val csvSchemaAndData = createCsvSchemaAndDataFromJson(jsonTree)
            val csvSchema = csvSchemaAndData.first
            val csvData = csvSchemaAndData.second
            val outputStream = ByteArrayOutputStream()

            val csvDataAsString =
                "sep=,\n" +
                    CsvMapper()
                        .writerFor(Map::class.java)
                        .with(csvSchema)
                        .writeValueAsString(csvData)
            objectMapper.writeValue(outputStream, csvDataAsString)
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
        }

        /**
         * Create a ByteStream to be used for JSON Export from CompanyAssociatedData.
         * @param companyAssociatedData passed companyAssociatedData to be exported
         * @return InputStreamResource byteStream for export.
         * Note that swagger only supports InputStreamResources and not OutputStreams
         */
        fun <T> buildJsonStreamFromCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>): InputStreamResource {
            val jsonTree: JsonNode = convertDataToJson(companyAssociatedData)
            val outputStream = ByteArrayOutputStream()

            objectMapper
                .writerFor(JsonNode::class.java)
                .writeValue(outputStream, jsonTree)
            return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
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
         * Creates the CSV schema and the CSV data used for CSV and Excel Export from JSON object
         * @param jsonNode The JSON node to create the CSV schema and data from
         * @return Pair of CsvSchema and a MutableMap<String, String> representing the CSV data
         */
        private fun createCsvSchemaAndDataFromJson(jsonNode: JsonNode): Pair<CsvSchema, MutableMap<String, String>> {
            val headers: List<String> = JsonUtils.getLeafNodeFieldNames(jsonNode)
            require(headers.isNotEmpty()) { "After filtering, CSV data is empty." }

            val csvData = mutableMapOf<String, String>()
            val csvSchemaBuilder = CsvSchema.builder()

            headers.forEach {
                csvData[it] = JsonUtils.getValueFromJsonNodeByPath(jsonNode, it)
                csvSchemaBuilder.addColumn(it)
            }

            val csvSchema = csvSchemaBuilder.build().withHeader()
            return Pair(csvSchema, csvData)
        }
    }
