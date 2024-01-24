package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to perform a single data request on Dataland.
 * @param companyIdentifier the company identifier for which the user wants to request framework data
 * @param frameworkName the name of the framework for which the user wants to request framework data
 * @param listOfReportingPeriods a list of reporting periods for which the user wants to request framework data
 * @param contactList a list of e-mail addresses related to the company to which a notification shall be sent
 * @param message a message that shall accompany the notification to the provided contacts
 */
data class SingleDataRequest(
    @field:JsonProperty(required = true)
    @field:Schema(example = "DE0005190003")
    val companyIdentifier: String,

    @field:JsonProperty(required = true)
    @field:Schema(example = "p2p")
    val frameworkName: DataTypeEnum,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"2022\", \"2023\"]",
        ),
        schema = Schema(type = "string"),
    )
    val listOfReportingPeriods: List<String>,

    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"testuser@dataland.com\"]",
        ),
        schema = Schema(type = "string"),
    )
    val contactList: List<String>?,

    val message: String?,
)
