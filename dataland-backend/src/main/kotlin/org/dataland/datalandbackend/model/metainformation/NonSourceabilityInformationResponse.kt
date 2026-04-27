package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * API response model representing a non-sourceability information record as returned by the backend.
 */
data class NonSourceabilityInformationResponse(
    @field:JsonProperty(required = true)
    @field:Schema(description = "Unique identifier for this non-sourceability entry.")
    val nonSourceabilityId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION)
    val dataType: DataType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(description = "Current QA review status of the non-sourceability entry.")
    val qaStatus: QaStatus,
    @field:JsonProperty(required = true)
    @field:Schema(description = "User ID of the user who submitted the non-sourceability request.")
    val uploaderUserId: String,
    @field:JsonProperty(required = true)
    @field:Schema(description = "Unix epoch millisecond timestamp when the entry was created.")
    val uploadTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "True when this entry is the active (accepted) non-sourceability record for the dataset.",
    )
    val currentlyActive: Boolean,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REASON_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "Whether this entry was created via the QA-bypass fast path (admin only).",
    )
    val bypassQa: Boolean,
)
