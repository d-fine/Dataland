package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

/**
 * The class to create csv file containing updated LEI-ISIN mapping
 */
@Component
class IsinDeltaBuilder {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates the delta file of the updated LEI-ISIN mapping file
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     * @param oldMappingFile previously stored version of the LEI-ISIN mapping file, if one exists
     */
    fun createDeltaOfMappingFile(newMappingFile: File, oldMappingFile: File): Map<String, String> {
        // Parse the old and new mapping csv files to get two maps of Lei -> ISIN
        val newMapping = parseCsvToGroupedMap(newMappingFile)
        val oldMapping = parseCsvToGroupedMap(oldMappingFile)
        val deltaMapping = findLeisWithUpdatedIsin(newMapping, oldMapping)
        replaceOldMappingFile(newMappingFile)
        return deltaMapping
    }

    /**
     * Compares the two LEI-ISIN maps and identifies LEIs that have new ISINs
     * @param newMap the new LEI-ISIN map
     * @param oldMap the old LEI-ISIN map
     * @return map of changed LEI-ISINs
     */
    fun findLeisWithUpdatedIsin(newMap: Map<String, String>, oldMap: Map<String, String>): Map<String, String> {
        // We group based on LEIs and turn ISINs into a comma-seperated string.
        // If a LEI exists in the new map but not in the old one, we add the LEI and its ISINs to delta.
        // If matching LEIs with different ISIN are found, we add the LEI with the new ISINs to delta.
        // Is it possible that only the order of LEIs change in the new file? The file is probably alphabetically
        // ordered anyway so its unlikely

        val deltaMap = mutableMapOf<String, String>()

        for ((lei, newIsins) in newMap) {
            val oldIsins = oldMap[lei]

            if (oldIsins == null || oldIsins != newIsins) {
                deltaMap[lei] = newIsins
            }
        }

        return deltaMap
    }

    /**
     * Coverts CSV file to a LEI-ISIN map, while also aggregating all ISINs of a specific
     * LEI into one comma-separated string
     * @param csvFile the file to be parsed
     * @return map of LEI-ISINs
     */
    private fun parseCsvToGroupedMap(csvFile: File): Map<String, String> {
        val csvMapper = CsvMapper()
        csvMapper.registerModule(kotlinModule())

        val csvSchema = CsvSchema.builder()
            .addColumn("LEI")
            .addColumn("ISIN")
            .setUseHeader(true)
            .build()

        val mappings = mutableMapOf<String, StringBuilder>()

        try {
            val csvParser = csvMapper
                .readerFor(Map::class.java)
                .with(csvSchema)
                .readValues<Map<String, String>>(csvFile)

            csvParser.forEach { entry ->
                val lei = entry["LEI"]
                val isin = entry["ISIN"]

                if (lei != null && isin != null && mappings.containsKey(lei)) {
                    mappings[lei]?.append(",")
                    mappings[lei]?.append(isin)
                } else if (lei != null && isin != null && !mappings.containsKey(lei)) {
                    mappings[lei] = StringBuilder(isin)
                }
            }
        } catch (e: FileSystemException) {
            logger.error("Error while parsing CSV: ${e.message}")
        }
        // Convert StringBuilder to String
        return mappings.mapValues { it.value.toString() }
    }

    /**
     * Replaces the locally saved old mapping file with the recently downloaded one after creating delta is done
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     */
    fun replaceOldMappingFile(newMappingFile: File) {
        // DO: Delete old mapping file and replace it with the new mapping file to be used next week
        newMappingFile.isFile
    }
}
