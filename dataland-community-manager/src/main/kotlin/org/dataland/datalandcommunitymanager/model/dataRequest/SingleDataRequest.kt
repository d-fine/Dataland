package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to perform a single data request on Dataland.
 * @param companyIdentifier the company identifier for which the user wants to request framework data
 * @param dataType the name of the framework for which the user wants to request framework data
 * @param reportingPeriods a set of reporting periods for which the user wants to request framework data
 * @param contacts a set of e-mail addresses related to the company to which a notification shall be sent
 * @param message a message that shall accompany the notification to the provided contacts
 */
data class SingleDataRequest(
    @field:JsonProperty(required = true)
    @field:Schema(example = "enterValidIdentifier")
    val companyIdentifier: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        implementation = DataTypeEnum::class,
        example = "p2p",
    )
    val dataType: DataTypeEnum,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                example = "[\"2022\", \"2023\"]",
            ),
    )
    val reportingPeriods: Set<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                example = "[\"testuser@example.com\"]",
            ),
    )
    val contacts: Set<String>?,
    val message: String?,
)
