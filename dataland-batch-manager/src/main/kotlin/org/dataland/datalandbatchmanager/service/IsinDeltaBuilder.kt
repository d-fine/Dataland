package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.stereotype.Component
import java.io.File

/**
 * The class to create csv file containing updated LEI-ISIN mapping
 */
@Component
class IsinDeltaBuilder {

    /**
     * Creates the delta file of the updated LEI-ISIN mapping file
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     * @param oldMappingFile previously stored version of the LEI-ISIN mapping file, if one exists
     */
    fun createDeltaOfMappingFile(newMappingFile: File, oldMappingFile: File?): Map<String, List<String>> {
        val newMapping = parseCsvToGroupedMap(newMappingFile)
        if (oldMappingFile == null) {
            return newMapping
        }
        val oldMapping = parseCsvToGroupedMap(oldMappingFile)
        return findLeisWithUpdatedIsin(newMapping, oldMapping)
    }

    /**
     * Compares the two LEI-ISIN maps and identifies LEIs that have new ISINs
     * @param newMapping the new LEI-ISIN mapping
     * @param oldMapping the old LEI-ISIN mapping
     * @return map of changed LEI-ISINs
     */
    private fun findLeisWithUpdatedIsin(
        newMapping: Map<String, List<String>>,
        oldMapping: Map<String, List<String>>,
    ): Map<String, List<String>> {
        val deltaMapping = mutableMapOf<String, List<String>>()

        for ((lei, newIsins) in newMapping) {
            val oldIsins = oldMapping[lei]

            if (oldIsins == null || oldIsins != newIsins) {
                deltaMapping[lei] = newIsins
            }
        }
        return deltaMapping
    }

    /**
     * Coverts CSV file to a LEI-ISIN map, while also aggregating all ISINs of a specific
     * LEI into a list
     * @param csvFile the file to be parsed
     * @return map of LEI-ISINs
     */
    private fun parseCsvToGroupedMap(csvFile: File): Map<String, List<String>> {
        val csvMapper = CsvMapper()
        csvMapper.registerModule(kotlinModule())

        val csvSchema = CsvSchema.builder()
            .addColumn("LEI_ISIN", CsvSchema.ColumnType.STRING)
            .setUseHeader(true)
            .build()

        val mappings = mutableMapOf<String, MutableList<String>>()

        val csvParser = csvMapper
            .readerFor(Map::class.java)
            .with(csvSchema)
            .readValues<Map<String, String>>(csvFile)

        csvParser.forEach { entry ->
            val data = entry["LEI_ISIN"]
            println("Data: $data") // Add this line for debugging
            if (data != null) {
                val values = data.split(",")
                if (values.size == 2) {
                    val lei = values[0].trim()
                    val isin = values[1].trim()

                    if (mappings.containsKey(lei)) {
                        mappings[lei]?.add(isin)
                    } else {
                        mappings[lei] = mutableListOf(isin)
                    }
                }
            }
        }

        return mappings
    }
}
