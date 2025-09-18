package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataDimensions
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * Data class that contains an instance of the abstract data dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType unique identifier for the abstract type of the data
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataDimensions(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    override val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
) : DataDimensions {
    /**
     * Converts the basic data dimensions to basic data point dimensions
     * @param dataPointType the type of the data point
     * @return the basic data point dimensions
     */
    fun toBasicDataPointDimensions(dataPointType: String = dataType) = BasicDataPointDimensions(companyId, dataPointType, reportingPeriod)
}
