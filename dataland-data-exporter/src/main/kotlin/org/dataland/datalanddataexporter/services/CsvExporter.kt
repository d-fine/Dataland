package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanddataexporter.utils.TransformationUtils.checkConsistency
import org.dataland.datalanddataexporter.utils.TransformationUtils.getHeaders
import org.dataland.datalanddataexporter.utils.TransformationUtils.getLeiToIsinMapping
import org.dataland.datalanddataexporter.utils.TransformationUtils.getTimestamp
import org.dataland.datalanddataexporter.utils.TransformationUtils.readTransformationConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_ID_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_NAME_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.ISIN_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_IDENTIFIER
import org.dataland.datalanddataexporter.utils.TransformationUtils.REPORTING_PERIOD_HEADER

/**
 * A class for handling the transformation of JSON files into CSV
 */
@Component("CsvExporter")
class CsvExporter(
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val sfdrDataControllerApi: SfdrDataControllerApi,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Value("\${dataland.data-exporter.output-directory}")
    private val outputDirectory: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /*private fun readJsonFileFromResourceFolder(): JsonNode {
        val input = this.javaClass.classLoader.getResourceAsStream("./src/main/resources/example.json")
        return ObjectMapper().readTree(input)
    }*/

    /*private fun readJsonFromString(): JsonNode {
        return ObjectMapper().readTree(jsonString)
    }*/

    /**
     * A dummy function that reads a JSON file from the resources folder,
     * transforms it into a CSV file and writes it to the resources folder.
     * @return A string message
     */
    // Remove this function and the complete REST API
    fun dummyFunction(): String {
        exportAllSfdrData()
        return "Hello World!"
    }

    /**
     * Gets all SFDR data IDs from the metadata endpoint in the backend.
     * @return A list of SFDR data IDs
     */
    fun getAllSfdrDataIds(): List<String> {
        val dataIds = mutableListOf<String>()
        val metaData = metaDataControllerApi.getListOfDataMetaInfo(dataType = DataTypeEnum.sfdr)
        metaData.forEach { dataIds.add(it.dataId) }
        return dataIds
    }

    /**
     * Exports all SFDR data and the associated LEI to ISIN mapping to a CSV file.
     */
    fun exportAllSfdrData() {
        logger.info("Exporting all SFDR data.")
        val timestamp = getTimestamp()
        val outputFile = File("$outputDirectory/data$timestamp.csv")
        val isinOutputFile = File("$outputDirectory/isin$timestamp.csv")
        logger.info("Writing to file: $outputFile")
        val csvData = mutableListOf<Map<String, String>>()
        val isinData = mutableListOf<Map<String, String>>()

        val transformationRules = readTransformationConfig("./transformationRules/SfdrSqlServer.config")
        val headers = getHeaders(transformationRules)
        val dataIds = getAllSfdrDataIds()

        dataIds.forEach { dataId ->
            logger.info("Exporting data with ID: $dataId")
            val dataToExport = mutableMapOf<String, String>()

            val companyAssociatedData = sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId)
            val companyData = companyDataControllerApi.getCompanyById(companyAssociatedData.companyId)

            dataToExport[REPORTING_PERIOD_HEADER] = companyAssociatedData.reportingPeriod
            dataToExport[COMPANY_ID_HEADER] = companyAssociatedData.companyId
            dataToExport[COMPANY_NAME_HEADER] = companyData.companyInformation.companyName
            val leiEntry = companyData.companyInformation.identifiers[LEI_IDENTIFIER] ?: emptyList()
            dataToExport[LEI_HEADER] = if (leiEntry.isEmpty()) "" else leiEntry[0]

            isinData.addAll(getLeiToIsinMapping(companyData.companyInformation))

            val objectMapper = jacksonObjectMapper().findAndRegisterModules()
            val jsonData = objectMapper.writeValueAsString(companyAssociatedData.data)
            val data = ObjectMapper().readTree(jsonData)
            checkConsistency(data, transformationRules)
            dataToExport += mapJsonToCsv(data, transformationRules)
            csvData.add(dataToExport)
        }
        logger.info("Writing results to CSV files.")
        writeCsv(csvData, outputFile, headers)
        writeCsv(isinData, isinOutputFile, listOf(LEI_HEADER, ISIN_HEADER))
    }

    /**
     * Maps a JSON node to a CSV.
     * @param jsonNode The JSON node
     * @param transformationRules The transformation rules
     * @return A map of CSV headers to values
     */
    fun mapJsonToCsv(jsonNode: JsonNode, transformationRules: Map<String, String>): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        transformationRules.forEach { (jsonPath, csvHeader) ->
            if (csvHeader.isEmpty()) return@forEach
            csvData[csvHeader] = getValueFromJsonNode(jsonNode, jsonPath)
        }
        return csvData
    }

    /**
     * Gets the string value of the JSON node identified by the (possibly) nested JSON path.
     * @param jsonNode The JSON node
     * @param jsonPath The JSON path identifying the value
     * @return The string representation of the value
     */
    fun getValueFromJsonNode(jsonNode: JsonNode, jsonPath: String): String {
        var currentNode = jsonNode
        jsonPath.split(".").forEach() { path ->
            currentNode = currentNode.get(path) ?: return ""
        }
        return if (currentNode.isTextual) {
            currentNode.textValue()
        } else {
            currentNode.toString()
        }
    }

    // Todo Add config object instead of passing the headers, file and separator?
    /**
     * Writes a CSV file.
     * @param data The data to write
     * @param outputFile The output file
     * @param headers The headers
     */
    fun writeCsv(data: List<Map<String, String>>, outputFile: File, headers: List<String>) {
        if (data.isEmpty()) return

        val csvSchemaBuilder = CsvSchema.builder()
        headers.forEach { header -> csvSchemaBuilder.addColumn(header) }
        val csvSchema = csvSchemaBuilder.build().withHeader().withColumnSeparator("|".first())

        CsvMapper().writerFor(List::class.java)
            .with(csvSchema)
            .writeValue(outputFile, data)
    }
}
