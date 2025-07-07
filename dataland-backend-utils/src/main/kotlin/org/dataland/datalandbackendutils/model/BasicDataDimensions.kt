package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataDimensions
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * Data class that contains an instance of the abstract data dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType unique identifier for the abstract type of the data
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataDimensions(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    override val dataType: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
) : DataDimensions {
    /**
     * Converts the basic data dimensions to basic data point dimensions
     * @param dataPointType the type of the data point
     * @return the basic data point dimensions
     */
    fun toBasicDataPointDimensions(dataPointType: String) = BasicDataPointDimensions(companyId, dataPointType, reportingPeriod)
}
