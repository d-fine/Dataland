package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState

/**
 * --- API model ---
 * Filter object to filter data requests when querying for existing requests.
 * All fields are optional. If a field is not provided, no filtering on this field is applied.
 * If a field is provided, only requests matching the filter criteria are returned.
 * This data class is generic to accommodate versions where companyId and userId are of type String or UUID.
 */
data class RequestSearchFilter<IdType>(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: IdType? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                description = GeneralOpenApiDescriptionsAndExamples.GENERAL_DATA_TYPES_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPES_FRAMEWORK_EXAMPLE,
            ),
        schema =
            Schema(
                type = "string",
            ),
    )
    val dataTypes: Set<String>? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                description = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_EXAMPLE,
            ),
        schema =
            Schema(
                type = "string",
            ),
    )
    val reportingPeriods: Set<String>? = null,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_REQUEST_USER_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: IdType? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATES_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATES_EXAMPLE,
            ),
        schema =
            Schema(
                implementation = RequestState::class,
            ),
    )
    val requestStates: Set<RequestState>? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITIES_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITIES_EXAMPLE,
            ),
        schema =
            Schema(
                implementation = RequestPriority::class,
            ),
    )
    val requestPriorities: Set<RequestPriority>? = null,
    val emailAddress: String? = null,
    val adminComment: String? = null,
    val companySearchString: String? = null,
)
