package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.File

/**
 * The class to create csv file containing updated LEI-ISIN mapping
 */
class IsinDeltaBuilder {

    /**
     * Creates the delta file of the updated LEI-ISIN mapping file
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     * @param oldMappingFile previously stored version of the LEI-ISIN mapping file, if one exists
     */
    fun createDeltaOfMappingFile(newMappingFile: File, oldMappingFile: File): Map<String, String> {
    //Parse the old and new mapping csv files to get two maps of Lei -> ISIN
        val newMapping = parseCsvToMap(newMappingFile)
        val oldMapping = parseCsvToMap(oldMappingFile)
        replaceOldMappingFile(newMappingFile)
        return findLeisWithUpdatedIsin(newMapping,oldMapping)
    }

    /**
     * Compares the two LEI-ISIN maps and identifies LEIs that have new ISINs
     * @param newMap the new LEI-ISIN map
     * @param oldMap the old LEI-ISIN map
     * @return map of changed LEI-ISINs
     */
    fun findLeisWithUpdatedIsin(newMap: Map<String, String>, oldMap: Map<String, String>): Map<String, String> {
    //We group based on LEIs and turn ISINs into a comma-seperated string.
    //If a LEI exists in the new map but not in the old one, we add the LEI and its ISINs to delta.
    //If matching LEIs with different ISIN are found, we add the LEI with the new ISINs to delta.
       // Kann es sein dass nur die reienfolge sich ändert?

        val deltaMap = mutableMapOf<String, String>()

        for ((lei,newIsins) in newMap) {
            val oldIsins = oldMap[lei]

            if (oldIsins == null || oldIsins != newIsins) {
                deltaMap[lei]= newIsins
            }
        }


        return deltaMap
    }

    //TODO: look if GleifCsvParser can be reused to some extent instead of this
    private fun parseCsvToMap(csvFile: File): Map<String, String> {
        val csvMapper = CsvMapper()
        csvMapper.registerModule(kotlinModule())

        val csvSchema = CsvSchema.builder()
            .addColumn("LEI")
            .addColumn("ISIN")
            .setUseHeader(true)
            .build()

        val mappings = mutableMapOf<String, String>()

        try {
            val csvParser = csvMapper
                .readerFor(Map::class.java)
                .with(csvSchema)
                .readValues<Map<String, String>>(csvFile)

            csvParser.forEach { entry ->
                val lei = entry["LEI"]
                val isin = entry["ISIN"]


                if (lei != null && isin != null) {
                    mappings[lei] = isin
                }
            }
        } catch (e: Exception) {
            // TODO: Handle exceptions
        }
            // TODO: maybe group mappings based on LEI here?
        return mappings
    }

    /**
     * Replaces the locally saved old mapping file with the recently downloaded one after creating delta is done
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     */
    fun replaceOldMappingFile(newFile: File) {
        //TODO: Delete old mapping file and replace it with the new mapping file to be used next week
    }
}