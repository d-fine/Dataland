package org.dataland.datalanddataexporter.services


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File
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
        return "Hello World!"
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
