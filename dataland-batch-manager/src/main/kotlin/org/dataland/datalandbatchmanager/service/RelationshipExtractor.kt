package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import org.dataland.datalandbatchmanager.model.GleifRelationshipInformation
import org.dataland.datalandbatchmanager.model.GleifRelationshipTypes
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * The class to create the list of mapping from child to parent
 */
@Component
class RelationshipExtractor {
    private val logger = LoggerFactory.getLogger(javaClass)
    var finalParentMapping = mutableMapOf<String, String>()

    /**
     *
     */
    fun prepareFinalParentMapping(gleifParser: MappingIterator<GleifRelationshipInformation>): Map<String, String> {
        val mappings = parseCsvToGroupedMap(gleifParser)

        val ultimateParentMapping = mappings[GleifRelationshipTypes.IS_ULTIMATELY_CONSOLIDATED_BY]
        val directParentMapping = mappings[GleifRelationshipTypes.IS_DIRECTLY_CONSOLIDATED_BY]
        val finalDirectParentMapping = findFinalDirectParent(directParentMapping)

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
    private fun findFinalDirectParent(directParentMapping: Map<String, String>?): Map<String, String> {
        val finalDirectParentMapping = mutableMapOf<String, String>()

        directParentMapping?.keys?.forEach { startNode ->
            val finalParent = findFinalDirectParentOneLei(startNode, directParentMapping)
            if (finalParent != null) {
                finalDirectParentMapping[startNode] = finalParent
            }
        }

        return finalDirectParentMapping
    }

    private fun findFinalDirectParentOneLei(startNode: String, directParentMapping: Map<String, String>): String? {
        val leisAlreadyTraversed: MutableSet<String> = mutableSetOf()
        var finalParent: String? = null
        var previousLEI = startNode
        leisAlreadyTraversed.add(startNode)

        while (true) {
            val targetLEI = directParentMapping.getOrElse(previousLEI) { null }
            val alreadyTraversed = (targetLEI in leisAlreadyTraversed)
            if ((targetLEI == null) || alreadyTraversed) {
                if (!alreadyTraversed) {
                    finalParent = previousLEI
                } else {
                    logger.info("Could not find final parent node because of circular references for LEI $startNode")
                }
                break
            }
            leisAlreadyTraversed.add(targetLEI)
            previousLEI = targetLEI
        }
        return finalParent
    }

    /**
     * Converts the zipped CSV file to a map of RelationshipType - (Map of startLEI - endLEI)
     * @param zipFile the file to be parsed
     */
    private fun parseCsvToGroupedMap(gleifParser: MappingIterator<GleifRelationshipInformation>):
        MutableMap<GleifRelationshipTypes, MutableMap<String, String>> {
        val mappings = mutableMapOf<GleifRelationshipTypes, MutableMap<String, String>>()
        gleifParser.forEach { entry ->
            val relationShipMap = mappings.getOrPut(entry.relationshipType) { mutableMapOf() }
            relationShipMap[entry.startNode] = entry.endNode
        }
        return mappings
    }
}
