package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

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
            implementation = DataTypeEnum::class,
            example = "[\"p2p\", \"vsme\"]",
        ),
    )
    val dataTypes: Set<DataTypeEnum>,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"2022\", \"2023\"]",
        ),
    )
    val reportingPeriods: Set<String>,
)
