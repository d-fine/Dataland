package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to perform a single data request on Dataland.
 * @param listOfCompanyIdentifiers contains company identifiers for which the user wants to request framework data
 * @param listOfFrameworkNames contains the names of frameworks, for which the user wants to request framework data
 */
data class SingleDataRequest(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"DE0005190003\", \"exampleForAnInvalidIdentifier\"]",
        ),
        schema = Schema(type = "string"),
    )
    val companyId: String,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            implementation = DataTypeEnum::class,
            example = "[\"p2p\"]",
        ),
    )
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

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"testuser@dataland.com\"]",
        ),
        schema = Schema(type = "string"),
    )
    val contactList: List<String>?,

    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(
            type = "string",
            example = "[\"testuser@dataland.com\"]",
        ),
        schema = Schema(type = "string"),
    )
    val message: String?,

)
