package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

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
        description = CompanyControllerDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.IS_NON_SOURCEABLE_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.IS_NON_SOURCEABLE_EXAMPLE,
    )
    val isNonSourceable: Boolean,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.REASON_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String,
)
