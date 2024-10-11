package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class containing the relevant information from the GLEIF Relationship csv files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GleifRelationshipInformation(
    @JsonProperty("Relationship.StartNode.NodeID")
    val startNode: String,
    @JsonProperty("Relationship.EndNode.NodeID")
    val endNode: String,
    @JsonProperty("Relationship.RelationshipType")
    val relationshipType: GleifRelationshipTypes,
)
