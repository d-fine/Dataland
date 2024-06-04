package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.model.GleifRelationshipTypes
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

/**
 * The class to create the list of mapping from child to parent
 */
@Component
class RelationshipExtractor(
    @Autowired private val gleifParser: GleifCsvParser,
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mappings = mutableMapOf<GleifRelationshipTypes, MutableMap<String, String>>()
    var finalParentMapping = mutableMapOf<String, String>()

    /**
     *
     */
    fun prepareFinalParentMapping(newMappingFile: File): Map<String, String> {
        parseCsvToGroupedMap(newMappingFile)

        val ultimateParentMapping = mappings[GleifRelationshipTypes.IS_ULTIMATELY_CONSOLIDATED_BY]
        val finalDirectParentMapping = findFinalDirectParent()

        if (ultimateParentMapping == null) {
            throw NotImplementedError("No GLEIF relationship information for IS_ULTIMATELY_CONSOLIDATED_BY found")
        }

        finalDirectParentMapping.keys.forEach {
            if (it !in ultimateParentMapping.keys) {
                finalDirectParentMapping[it]?.let { parentLei -> ultimateParentMapping.put(it, parentLei) }
            }
        }

        finalParentMapping = ultimateParentMapping

        return ultimateParentMapping
    }

    /**
     * Iterate through chain of direct parents to find final parent.
     * If infinite loop encountered: ignore it
     */
    private fun findFinalDirectParent(): Map<String, String> {
        val finalDirectParentMapping = mutableMapOf<String, String>()

        val directParentMapping = mappings[GleifRelationshipTypes.IS_DIRECTLY_CONSOLIDATED_BY]

        directParentMapping?.keys?.forEach { startNode ->

            val leisAlreadyTraversed: Set<String> = emptySet()
            var previousLEI = startNode

            while (true) {
                val targetLEI = directParentMapping.getOrElse(previousLEI) { null }
                if (targetLEI == null) {
                    finalDirectParentMapping[startNode] = previousLEI
                    break
                } else {
                    if (targetLEI in leisAlreadyTraversed) {
                        logger.info("Could not find final parent node because of infinite loop for LEI $startNode")
                        break
                    }
                    leisAlreadyTraversed.plus(targetLEI)
                    previousLEI = targetLEI
                }
            }
        }

        return finalDirectParentMapping
    }

    /**
     * Coverts CSV file to a map of RelationshipType - (Map of startLEI - endLEI)
     * @param csvFile the file to be parsed
     */
    private fun parseCsvToGroupedMap(zipFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(zipFile)
        val gleifCsvParser = gleifParser.readGleifRelationshipDataFromBufferedReader(gleifDataStream)

        gleifCsvParser.forEach { entry ->
            val relationShipMap = mappings.getOrPut(entry.relationshipType) { mutableMapOf() }
            relationShipMap[entry.startNode] = entry.endNode
        }
    }
}
