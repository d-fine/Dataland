package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

/**
 * --- API model ---
 * SourceabilityInfoResponse is used for api response (get requests). Returns information regarding whether data for a
 * triple (companyId, dataType, reportingPeriod) is available.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param isNonSourceable true if there is no source available
 * @param reason reason why there is no source available
 * @param userId user who uploaded information on the sourceability of the date set
 * @param creationTime time when the info has been posted
 */
data class SourceabilityInfoResponse(
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
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.CREATION_TIME_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.CREATION_TIME_EXAMPLE,
    )
    val creationTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val userId: String,
)
