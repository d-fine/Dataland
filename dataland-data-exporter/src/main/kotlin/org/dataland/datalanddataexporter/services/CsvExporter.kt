package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import java.io.File
import org.dataland.datalanddataexporter.utils.TransformationUtils.getTimestamp
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanddataexporter.utils.TransformationUtils.checkConsistency
import org.dataland.datalanddataexporter.utils.TransformationUtils.getHeaders
import org.dataland.datalanddataexporter.utils.TransformationUtils.getLeiToIsinMapping
import org.dataland.datalanddataexporter.utils.TransformationUtils.readTransformationConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

/**
 * A class for handling the transformation of JSON files into CSV
 */
@Component("CsvExporter")
class CsvExporter(
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val sfdrDataControllerApi: SfdrDataControllerApi,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Value("\${dataland.data-exporter.output-directory}")
    private val outputDirectory: String
) {


    private val logger = LoggerFactory.getLogger(javaClass)

    //test
    private val leiIdentifier = "Lei"
    //private val isinIdentifier = "Isin"

    private val leiHeader = "LEI"
    //private val leiPath = "Lei"
    private val isinHeader = "ISIN"
    //private val isinPath = "isin"

    /*private fun readJsonFileFromResourceFolder(): JsonNode {
        val input = this.javaClass.classLoader.getResourceAsStream("./src/main/resources/example.json")
        return ObjectMapper().readTree(input)
    }*/

    /*private fun readJsonFromString(): JsonNode {
        return ObjectMapper().readTree(jsonString)
    }*/

    /*private val jsonString = """
        {
  "companyId": "e7a6f3cb-118f-4939-b39a-48e48047d028",
  "companyName": "Fresenius SE & Co. KGaA",
  "Lei": "XDFJ0CYCOO1FXRFTQS51",
  "number": 123,
  "isin": [
    "DE0005785604",
    "US0005785604"
  ],
  "nested": {
    "nestedCompanyId": {
      "deepNestedCompanyId": "123"
    },
    "arrayExample": [
      {
        "nestedArrayCompanyId": "1",
        "nestedArrayCompanyName": "Hallo1",
        "nestedArrayLei": "2"
      },
      {
        "nestedArrayCompanyId": "3",
        "nestedArrayCompanyName": "Hallo2",
        "nestedArrayLei": "4"
      }
    ],
    "nestedCompanyName": "Hallo",
    "nestedLei": "12344"
  }
}
    """.trimIndent()
*/

    /**
     * A dummy function that reads a JSON file from the resources folder,
     * transforms it into a CSV file and writes it to the resources folder.
     * @return A string message
     */
    fun dummyFunction(): String {
        //val leiToIsinMapping = mutableMapOf<String, List<String>>()
        //val leiToIsinData = mutableListOf(mapOf<String, String>())
        //val jsonNode = readJsonFileFromResourceFolder()
        //val jsonNode = readJsonFromString()
        //val transformationRules = readTransformationConfig("transformation.config")
        //val outputFile = File("./src/main/resources/data.csv")
        //val outputFile = File("/var/export/data.csv")
        //val isinOutputFile = File("./src/main/resources/isin.csv")
        //val isinOutputFile = File("/var/export/isin.csv")
        //val csvData = mapJsonToCsv(jsonNode, transformationRules)
        //val headers = getHeaders(transformationRules)
        //leiToIsinMapping+=getLeiToIsinData(jsonNode, "Lei", "isin")
        //leiToIsinData.addAll(getLeiToIsinData(jsonNode))
        /*val isinData= mutableListOf(mapOf<String, String>())
        leiToIsinMapping.forEach { (lei, isins) ->
            isins.forEach { isin ->
                isinData.add(mapOf("LEI" to lei, "ISIN" to isin))
                println("LEI: $lei ISIN: $isin") }
        }*/
        //checkConsistency(jsonNode, transformationRules)
        //writeCsv(listOf(csvData), outputFile, headers)
        //writeCsv(leiToIsinData, isinOutputFile, listOf(leiHeader, isinHeader))
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

    fun exportAllSfdrData() {
        logger.info("Exporting all SFDR data.")
        val timestamp = getTimestamp()
        val outputFile = File("$outputDirectory/data$timestamp.csv")
        val isinOutputFile = File("$outputDirectory/isin$timestamp.csv")
        logger.info("Writing to file: ${outputFile.absolutePath}")
        val csvData = mutableListOf<Map<String, String>>()
        val isinData = mutableListOf<Map<String, String>>()

        val transformationRules = readTransformationConfig("transformation.config")
        val headers = getHeaders(transformationRules)
        val dataIds = getAllSfdrDataIds()

        dataIds.forEach { dataId ->
            logger.info("Exporting data with ID: $dataId")
            val dataToExport = mutableMapOf<String, String>()


            val companyAssociatedData = sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId)
            val companyData = companyDataControllerApi.getCompanyById(companyAssociatedData.companyId)

            dataToExport["reportingPeriod"] = companyAssociatedData.reportingPeriod
            dataToExport["companyId"] = companyAssociatedData.companyId
            dataToExport["companyName"] = companyData.companyInformation.companyName
            val lei = companyData.companyInformation.identifiers[leiIdentifier] ?: emptyList()
            dataToExport["lei"] = if (lei.isEmpty()) "" else lei[0]

            isinData.addAll(getLeiToIsinMapping(companyData.companyInformation))

            val jsonData = ObjectMapper().writeValueAsString(companyAssociatedData.data)
            val data = ObjectMapper().readTree(jsonData)
            checkConsistency(data, transformationRules)
            dataToExport+=mapJsonToCsv(data, transformationRules)
            csvData.add(dataToExport)
        }
        logger.info("Writing results to CSV files.")
        writeCsv(csvData, outputFile, headers)
        writeCsv(isinData, isinOutputFile, listOf(leiHeader, isinHeader))
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

    /*/**
     * Extracts the LEI and ISINs identified by their respective JSON path from the given JSON node.
     * @param node The JSON node
     * @return A map of LEI to ISINs
     */
    fun getLeiToIsinData(node: JsonNode): List<Map<String, String>> {
        val isinData = mutableListOf<Map<String, String>>()
        val lei = getValueFromJsonNode(node, leiPath)
        val isins = getValueFromJsonNode(node, isinPath)
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().trim('"') }
        require(lei.isNotBlank() and isins.isNotEmpty()) { "LEI or ISINs not found in provided JSON node." }
        isins.forEach { isin ->
            isinData.add(mapOf(leiHeader to lei, isinHeader to isin))
        }
        return isinData
    }*/
}
