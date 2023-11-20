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
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}") private val savedMappingFile: File,
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
     * @param newMapping the new LEI-ISIN mapping
     * @param oldMapping the old LEI-ISIN mapping
     * @return map of changed LEI-ISINs
     */
    private fun findLeisWithUpdatedIsin(newMapping: Map<String, String>, oldMapping: Map<String, String>): Map<String, String> {
        val deltaMapping = mutableMapOf<String, String>()

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
                }
                if (lei != null && isin != null && !mappings.containsKey(lei)) {
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
            newMappingFile.copyTo(File(savedMappingFile.parent, "isinMapping.csv"), true)
            if (!newMappingFile.delete()) {
                logger.error("failed to delete file $newMappingFile")
            }
        } catch (e: FileSystemException) {
            logger.error("Error while replacing the old mapping file: ${e.message}")
        }
    }
}
