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
        println("Hello World!")
        val test = readJsonFileFromResourceFolder()
        println(test.toPrettyString())

        val testFile = readJsonFileFromResourceFolder()

        val testTransformation = readTransformationConfig("transformation.config")
        println("Transformation rules:")
        println(testTransformation.toString())
        val csvData = mapJsonToCsv(testFile, testTransformation)
        println(csvData.toString())
        //writeCsv(listOf(csvData), "./src/main/resources/output.csv")
        writeCsv(csvData, "./src/main/resources/output.csv")
        return "Hello World!"
    }

    fun readTransformationConfig(fileName: String): Map<String, String> {
        val props = Properties()
        props.load(this.javaClass.classLoader.getResourceAsStream(fileName))
        return props
            .map { (jsonPath, csvHeader) -> jsonPath.toString() to csvHeader.toString() }
            .toMap()
    }

    fun mapJsonToCsv(jsonNode: JsonNode, transformationRules: Map<String, String>): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        transformationRules.forEach { (jsonPath, csvHeader) ->
            println("Key: $jsonPath, Value: $csvHeader")
            if (csvHeader == "skipped") {
                println("Skipping $jsonPath")
                return@forEach
            }
            if (jsonNode.get(jsonPath) == null) {
                throw IllegalArgumentException("Expected JSON path $jsonPath to exist but it was not found.")
            }
            csvData[csvHeader] = jsonNode.get(jsonPath).toString()

        }
        return csvData
    }

    fun writeCsv(data: Map<String, String>, outputFilePath: String) {
        val csvMapper = CsvMapper()
        val csvFile = File(outputFilePath)
        val csvSchemaBuilder = CsvSchema.builder()


        data.keys.forEach { header -> csvSchemaBuilder.addColumn(header) }

        val csvSchema = csvSchemaBuilder.build().withHeader().withColumnSeparator("|".first())
        csvMapper.writerFor(Map::class.java)
            .with(csvSchema)
            .writeValue(csvFile, data)
    }

    fun writeCsv(data: List<Map<String, String>>, outputFilePath: String) {
        if (data.isEmpty()) return

        val csvSchemaBuilder = CsvSchema.builder()
        val headers = mutableSetOf<String>()

        data.forEach { row ->
            row.keys.forEach { key ->
                headers.add(key)
            }
        }
        headers.forEach { header -> csvSchemaBuilder.addColumn(header) }

        val csvSchema = csvSchemaBuilder.build().withHeader().withColumnSeparator("|".first())

        val csvMapper = CsvMapper()
        val csvFile = File(outputFilePath)
        csvMapper.writerFor(List::class.java)
            .with(csvSchema)
            .writeValue(csvFile, data)
    }
}
