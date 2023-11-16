package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

/**
 * The class to create csv file containing updated LEI-ISIN mapping
 */
@Component
class IsinDeltaBuilder(
    @Value("\${dataland.dataland-batch-manager.mapping-file}") private val savedMappingFile: File,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates the delta file of the updated LEI-ISIN mapping file
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     * @param oldMappingFile previously stored version of the LEI-ISIN mapping file, if one exists
     */
    fun createDeltaOfMappingFile(newMappingFile: File, oldMappingFile: File?): Map<String, String> {
        val newMapping = parseCsvToGroupedMap(newMappingFile)
        if (oldMappingFile == null) {
            replaceOldMappingFile(newMappingFile)
            return newMapping
        }
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
        try {
            if (savedMappingFile.exists() && !savedMappingFile.delete()) {
                logger.error("Unable to delete the old mapping file $savedMappingFile")
                return
            }
            val renamedFile = File(savedMappingFile.parent, "isinMapping.csv")
            newMappingFile.copyTo(renamedFile, true)
            newMappingFile.delete()
        } catch (e: FileSystemException) {
            logger.error("Error while replacing the old mapping file: ${e.message}")
        }
    }
}
