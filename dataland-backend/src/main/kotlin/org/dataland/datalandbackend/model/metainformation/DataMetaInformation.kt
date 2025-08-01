package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

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
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val dataId: String,
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
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val uploaderUserId: String? = null,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CURRENTLY_ACTIVE_DESCRIPTION,
    )
    val currentlyActive: Boolean,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    var qaStatus: QaStatus,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REF_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REF_EXAMPLE,
        nullable = true,
    )
    var ref: String? = null,
) {
    /**
     * Adds a reference URL to the data meta information points to the page displaying the specified dataset.
     * @param proxyPrimaryUrl the primary URL of the proxy server
     */
    fun addRef(proxyPrimaryUrl: String) {
        this.ref = "https://$proxyPrimaryUrl/companies/$companyId/frameworks/$dataType/$dataId"
    }
}
