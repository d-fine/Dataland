package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Contains all relevant info that a user receives regarding already existing datasets

 */
data class ResourceResponse(
    @field:JsonProperty(required = true)
    val userProvidedIdentifier: String,
    @field:JsonProperty(required = true)
    val companyName: String,
    @field:JsonProperty(required = true)
    val framework: String,
    @field:JsonProperty(required = true)
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    val resourceId: String,
    @field:JsonProperty(required = true)
    val resourceUrl: String,
)
