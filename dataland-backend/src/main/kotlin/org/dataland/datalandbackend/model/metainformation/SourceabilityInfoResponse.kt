package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.util.UUID

/**
 * --- API model ---
 * SourceabilityInfoResponse is used for API response (get requests).
 * It now covers non-sourceability lifecycle semantics and retains legacy fields for backward compatibility.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param reason reason why there is no source available
 * @param nonSourceabilityId unique identifier for non-sourceability request
 * @param uploaderUserId uploader user ID in non-sourceability workflow
 * @param uploadTime upload time in epoch milliseconds for non-sourceability workflow
 * @param qaStatus QA status for non-sourceability workflow
 * @param currentlyActive whether this non-sourceability record is currently active
 * @param isNonSourceable legacy field kept for compatibility
 * @param creationTime legacy field kept for compatibility
 * @param userId legacy field kept for compatibility
 */
data class SourceabilityInfoResponse(
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
        description = BackendOpenApiDescriptionsAndExamples.REASON_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_EXAMPLE,
    )
    val creationTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_NON_SOURCEABLE_DESCRIPTION,
    )
    val isNonSourceable: Boolean,
    @field:JsonProperty(required = false)
    val nonSourceabilityId: UUID? = null,
    @field:JsonProperty(required = false)
    val uploaderUserId: String? = null,
    @field:JsonProperty(required = false)
    val uploadTime: Long? = null,
    @field:JsonProperty(required = false)
    val qaStatus: String? = null,
    @field:JsonProperty(required = false)
    val currentlyActive: Boolean? = null,
)
