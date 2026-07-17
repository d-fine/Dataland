package org.dataland.datalandbackend.model

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * Request body for searching viewable data dimensions by filter.
 *
 * Each field is optional, but at least one list must not be empty.
 * A null value is treated as a wildcard (all values for that dimension).
 *
 * @param companyIds the company IDs to filter by; null or absent means all companies
 * @param dataTypes the frameworks or data point types to filter by;
 *   null or absent means all types
 * @param reportingPeriods the reporting periods to filter by; null or absent means all periods
 */
data class DataDimensionSearchRequest(
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_EXAMPLE,
            ),
    )
    val companyIds: List<String>? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.DATA_TYPES_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.DATA_TYPES_EXAMPLE,
            ),
    )
    val dataTypes: List<String>? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_EXAMPLE,
            ),
    )
    val reportingPeriods: List<String>? = null,
) {
    /**
     * Converts this API request to the internal [DataDimensionQuery] used by the service layer.
     * Null fields are converted to empty lists, which the service layer treats as wildcards.
     */
    fun toDataDimensionQuery(): DataDimensionQuery =
        DataDimensionQuery(
            companyIds = companyIds.orEmpty(),
            dataTypes = dataTypes.orEmpty(),
            reportingPeriods = reportingPeriods.orEmpty(),
        )
}
