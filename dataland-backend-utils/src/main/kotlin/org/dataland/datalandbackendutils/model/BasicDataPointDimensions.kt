package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * Data class that contains an instance of the data point dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataPointType unique identifier for the type of data point itself
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataPointDimensions(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DATA_POINT_TYPE_EXAMPLE,
    )
    override val dataPointType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
) : DataPointDimensions
