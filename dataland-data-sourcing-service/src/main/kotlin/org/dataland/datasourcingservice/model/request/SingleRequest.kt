package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to perform a single data request on Dataland.
 * @param companyIdentifier the company identifier for which the user wants to request framework data
 * @param dataType the name of the framework for which the user wants to request framework data
 * @param reportingPeriods a reporting periods for which the user wants to request framework data
 * @param comment a free text comment provided by the user
 */
data class SingleRequest(
    @field:JsonProperty(required = true)
    @field:Schema(
        type = "string",
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_EXAMPLE,
    )
    val companyIdentifier: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        type = "string",
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    val dataType: String,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_EXAMPLE,
            ),
    )
    val reportingPeriods: Set<String>,
    @field:Schema(
        type = "boolean",
        description = GeneralOpenApiDescriptionsAndExamples.SINGLE_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION,
    )
    val notifyMeImmediately: Boolean,
    @field:JsonProperty(required = false)
    @field:Schema(
        type = "string",
        description = DataSourcingOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.COMMENT_EXAMPLE,
    )
    val comment: String?,
)
