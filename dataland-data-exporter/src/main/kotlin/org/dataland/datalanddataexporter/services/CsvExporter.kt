package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File
import java.util.Properties
import org.springframework.stereotype.Component

/**
 * A class for handling the transformation of JSON files into CSV
 */
@Component("CsvExporter")
class CsvExporter {

    private fun readJsonFileFromResourceFolder(): JsonNode {
        val inputFilePath = "./src/main/resources/example.json"
        return ObjectMapper().readTree(File(inputFilePath))
    }

    fun dummyFunction(): String {
        val jsonNode = readJsonFileFromResourceFolder()
        val transformationRules = readTransformationConfig("transformation.config")
        val outputFile = File("./src/main/resources/output.csv")
        val csvData = mapJsonToCsv(jsonNode, transformationRules)
        val headers = getHeaders(transformationRules)
        writeCsv(listOf(csvData), outputFile, headers)
        return "Hello World!"
    }

    fun readTransformationConfig(fileName: String): Map<String, String> {
        val props = Properties()
        props.load(this.javaClass.classLoader.getResourceAsStream(fileName))
        return props
            .map { (jsonPath, csvHeader) -> jsonPath.toString() to csvHeader.toString() }
            .toMap()
    }

    fun getHeaders(transformationRules: Map<String, String>): List<String> {
        val headers = mutableListOf<String>()
        transformationRules.forEach { (_, csvHeader) -> if (csvHeader.isNotEmpty()) headers.add(csvHeader) }
        if (headers.distinct().size != headers.size) {
            throw IllegalArgumentException("Duplicate headers found in transformation rules.")
        }
        return headers
    }

    fun mapJsonToCsv(jsonNode: JsonNode, transformationRules: Map<String, String>): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        transformationRules.forEach { (jsonPath, csvHeader) ->
            if (csvHeader.isEmpty()) return@forEach
            if (jsonNode.get(jsonPath) == null) {
                csvData[csvHeader] = ""
            } else {
                csvData[csvHeader] = jsonNode.get(jsonPath).textValue()
            }
        }
        return csvData
    }

    // Todo Add config object instead of passing the headers, file and separator?
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