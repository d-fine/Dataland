package org.dataland.datalandbackend.model.dataavailability

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * Request body for querying available data dimensions by filter. At least one of the lists must be filled.
 *
 * @param companyIds the company IDs to filter by; an empty list serves as a wildcard.
 * @param frameworksOrDataPointTypes the frameworks or data point types to filter by; an empty list serves as a wildcard.
 * @param reportingPeriods the reporting periods to filter by; an empty list is treated as a wildcard (all periods)
 */
data class DataAvailabilitySearchRequest(
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_EXAMPLE,
            ),
    )
    val companyIds: List<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.FRAMEWORKS_OR_DATA_POINT_TYPES_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.FRAMEWORKS_OR_DATA_POINT_TYPES_EXAMPLE,
            ),
    )
    val frameworksOrDataPointTypes: List<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_EXAMPLE,
            ),
    )
    val reportingPeriods: List<String>,
)
