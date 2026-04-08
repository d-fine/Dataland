package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType

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
    @field:Schema(
        description = "Canonical non-sourceability record id used for cross-service correlation.",
        example = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val nonSourceabilityId: String? = null,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val qaStatus: QaStatus? = null,
    @field:Schema(
        description = "Indicates whether the non-sourceability claim is currently active.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val currentlyActive: Boolean? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.BYPASS_QA_DESCRIPTION,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val bypassQa: Boolean? = null,
    @field:Schema(
        description = "Lifecycle event type used by asynchronous non-sourceability consumers.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val eventType: NonSourceabilityEventType? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val uploaderUserId: String? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_EXAMPLE,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val uploadTime: Long? = null,
)
