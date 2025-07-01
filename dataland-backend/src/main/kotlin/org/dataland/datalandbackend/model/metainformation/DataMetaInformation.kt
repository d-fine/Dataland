package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param uploaderUserId the user ID of the user who requested the upload of this dataset
 * @param uploadTime is a timestamp for the upload of this dataset
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param ref direct link to the page displaying the specified dataset
 */
data class DataMetaInformation(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val dataId: String,
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
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val uploaderUserId: String? = null,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.CURRENTLY_ACTIVE_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.CURRENTLY_ACTIVE_EXAMPLE,
    )
    val currentlyActive: Boolean,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    var qaStatus: QaStatus,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.REF_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.REF_EXAMPLE,
        nullable = true,
    )
    var ref: String? = null,
)
