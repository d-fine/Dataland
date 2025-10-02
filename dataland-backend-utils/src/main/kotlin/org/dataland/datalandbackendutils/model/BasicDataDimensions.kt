package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataDimensions
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * Data class that contains an instance of the abstract data dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType unique identifier for the abstract type of the data
 * @param reportingPeriod the year for which the data was provided
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
     * Converts the basic data dimensions object to a basic data set dimensions object
     * @param framework the framework of the dataset the dimensions shall be converted to, defaults to the data type
     * @return the basic dataset dimensions object
     */
    fun toBasicDataSetDimensions(framework: String = dataType) =
        BasicDataSetDimensions(companyId = companyId, framework = framework, reportingPeriod = reportingPeriod)
}
