package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to request a bulk of datasets on Dataland.
 * @param companyIdentifiers contains company identifiers for which the user wants to request framework data
 * @param dataTypes contains the names of frameworks, for which the user wants to request framework data
 */
data class BulkDataRequest(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"enterValidIdentifiers\"]",
        ),
    )
    val companyIdentifiers: Set<String>,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"p2p\", \"sme\"]",
        ),
    )
    val dataTypes: Set<String>,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"2022\", \"2023\"]",
        ),
    )
    val reportingPeriods: Set<String>,
)
