package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * SourceabilityInfo storing the history of whether a dataset is sourceable or not used for posting and for message queue.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param isNonSourceable true if there is no source available
 * @param reason reason why there is no source available
 */
data class SourceabilityInfo(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_NON_SOURCEABLE_DESCRIPTION,
    )
    val isNonSourceable: Boolean,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REASON_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String,
)
