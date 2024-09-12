package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import java.io.File
import java.util.Properties

/**
 * A class for handling the transformation of JSON files into CSV
 */
@Component("CsvExporter")
class CsvExporter {

    private fun readJsonFileFromResourceFolder(): JsonNode {
        val inputFilePath = "./src/main/resources/example.json"
        return ObjectMapper().readTree(File(inputFilePath))
    }

    /**
     * A dummy function that reads a JSON file from the resources folder,
     * transforms it into a CSV file and writes it to the resources folder.
     * @return A string message
     */
    fun dummyFunction(): String {
        val jsonNode = readJsonFileFromResourceFolder()
        val transformationRules = readTransformationConfig("transformation.config")
        val outputFile = File("./src/main/resources/output.csv")
        val csvData = mapJsonToCsv(jsonNode, transformationRules)
        val headers = getHeaders(transformationRules)
        writeCsv(listOf(csvData), outputFile, headers)
        return "Hello World!"
    }

    /**
     * Reads a transformation configuration file and returns a map of JSON paths to CSV headers.
     * @param fileName The name of the transformation configuration file
     * @return A map of JSON paths to CSV headers
     */
    fun readTransformationConfig(fileName: String): Map<String, String> {
        val props = Properties()
        props.load(this.javaClass.classLoader.getResourceAsStream(fileName))
        return props
            .map { (jsonPath, csvHeader) -> jsonPath.toString() to csvHeader.toString() }
            .toMap()
    }

    /**
     * Gets the headers from the transformation rules.
     * @param transformationRules The transformation rules
     * @return A list of headers
     */
    fun getHeaders(transformationRules: Map<String, String>): List<String> {
        val headers = mutableListOf<String>()
        transformationRules.forEach { (_, csvHeader) -> if (csvHeader.isNotEmpty()) headers.add(csvHeader) }
        require(headers.isNotEmpty()) { "No headers found in transformation rules." }
        require(headers.distinct().size == headers.size) { "Duplicate headers found in transformation rules." }
        return headers
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

    fun getValueFromJsonNode(jsonNode: JsonNode, jsonPath: String): String {
        var currentNode = jsonNode
        jsonPath.split(".").forEach() { path ->
            currentNode = currentNode.get(path) ?: return ""
        }
        return currentNode.textValue()
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
