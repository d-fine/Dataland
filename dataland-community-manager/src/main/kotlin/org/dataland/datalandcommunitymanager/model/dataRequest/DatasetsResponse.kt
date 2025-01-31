package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Contains all relevant info that a user receives regarding already existing datasets

 */
data class DatasetsResponse(
    @field:JsonProperty(required = true)
    val userProvidedCompanyId: String,
    @field:JsonProperty(required = true)
    val companyName: String,
    @field:JsonProperty(required = true)
    val framework: String,
    @field:JsonProperty(required = true)
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    val datasetId: String,
    @field:JsonProperty(required = true)
    val datasetUrl: String,
)
