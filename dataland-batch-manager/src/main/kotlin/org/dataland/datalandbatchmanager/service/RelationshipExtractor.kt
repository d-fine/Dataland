package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.model.GleifRelationshipInformation
import org.dataland.datalandbatchmanager.model.GleifRelationshipTypes
import org.springframework.stereotype.Component

/**
 * The class to create the list of mapping from child to parent
 */
@Component
class RelationshipExtractor {
    var finalParentMapping = mutableMapOf<String, String>()

    /**
     *  Converts the return object of the relationship file csv parser to a final map of child lei - parent lei
     *  according to a simple logic.
     *  @return The final mapping of child LEI to parent LEI as MutableMap<String, String>
     */
    fun prepareFinalParentMapping(gleifParser: Iterable<GleifRelationshipInformation>): Map<String, String> {
        val mappings = parseCsvToGroupedMap(gleifParser)

        val localFinalParentMapping = mappings[GleifRelationshipTypes.IS_ULTIMATELY_CONSOLIDATED_BY] ?: mutableMapOf()
        val orderOfImportance = listOf(
            GleifRelationshipTypes.IS_DIRECTLY_CONSOLIDATED_BY,
            GleifRelationshipTypes.IS_FUNDMANAGED_BY,
            GleifRelationshipTypes.IS_SUBFUND_OF,
            GleifRelationshipTypes.IS_INTERNATIONAL_BRANCH_OF,
            GleifRelationshipTypes.IS_FEEDER_TO,
        )

        orderOfImportance.forEach { relationshipType ->
            val relationshipMap = mappings[relationshipType]
            loopAndAddToFinalParentMapIfNotPresent(relationshipMap, localFinalParentMapping)
        }
        finalParentMapping = localFinalParentMapping

        return localFinalParentMapping
    }

    private fun loopAndAddToFinalParentMapIfNotPresent(
        relationshipMap: MutableMap<String, String>?,
        localFinalParentMapping: MutableMap<String, String>,
    ) {
        relationshipMap?.keys?.forEach { startLei ->
            if (startLei !in localFinalParentMapping) {
                relationshipMap[startLei]?.let { parentLei -> localFinalParentMapping[startLei] = parentLei }
            }
        }
    }

    /**
     * Converts iterator of GleifRelationshipInformation to a map of start LEI to end LEI for each
     * relationship type: Map of RelationshipType - (Map of startLEI - endLEI)
     * @param gleifParser The iterator returned from the csv reader to loop through the GleifRelationshipInformation
     */
    private fun parseCsvToGroupedMap(gleifParser: Iterable<GleifRelationshipInformation>):
        MutableMap<GleifRelationshipTypes, MutableMap<String, String>> {
        val mappings = mutableMapOf<GleifRelationshipTypes, MutableMap<String, String>>()
        gleifParser.forEach { entry ->
            val relationShipMap = mappings.getOrPut(entry.relationshipType) { mutableMapOf() }
            relationShipMap[entry.startNode] = entry.endNode
        }
        return mappings
    }
}
