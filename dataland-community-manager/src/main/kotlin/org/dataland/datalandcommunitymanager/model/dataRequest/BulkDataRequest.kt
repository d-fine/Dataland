package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to request a bulk of datasets on Dataland.
 * @param listOfCompanyIdentifiers contains company identifiers for which the user wants to request framework data
 * @param listOfFrameworkNames contains the names of frameworks, for which the user wants to request framework data
 */
data class BulkDataRequest(
    @field:JsonProperty(required = true)
    val listOfCompanyIdentifiers: List<String>,

    // TODO Problem: The Deserializer somehow expects "eutaxonomyminusfinancials" instead of "eutaxonomy-financials"
    @field:JsonProperty(required = true)
    val listOfFrameworkNames: List<DataTypeEnum>,
)
