package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

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
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_EXAMPLE,
    )
    val companyIdentifier: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        implementation = DataTypeEnum::class,
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataTypeEnum,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.BULK_REQUEST_REPORTING_PERIODS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.BULK_REQUEST_REPORTING_PERIODS_EXAMPLE,
            ),
    )
    val reportingPeriods: Set<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_EXAMPLE,
            ),
    )
    val contacts: Set<String>?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_MESSAGE_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_MESSAGE_EXAMPLE,
    )
    val message: String?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.SINGLE_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION,
    )
    val notifyMeImmediately: Boolean = false,
)
