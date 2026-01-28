package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import java.util.UUID

/**
 * DTO for creating and transferring request data.
 */
data class StoredRequest(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_ID_EXAMPLE,
    )
    val id: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
    )
    val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_REQUEST_USER_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_CREATION_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.COMMENT_EXAMPLE,
        nullable = true,
    )
    val memberComment: String? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        nullable = true,
    )
    val adminComment: String? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_EXAMPLE,
    )
    val lastModifiedDate: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_EXAMPLE,
    )
    val requestPriority: RequestPriority,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_EXAMPLE,
    )
    val state: RequestState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        nullable = true,
    )
    val dataSourcingEntityId: String? = null,
) {
    /**
     * Converts this StoredDataRequest to a RequestEntity.
     *
     * @return The corresponding RequestEntity.
     */
    fun toRequestEntity(): RequestEntity =
        RequestEntity(
            id = UUID.fromString(this.id),
            companyId = UUID.fromString(this.companyId),
            reportingPeriod = this.reportingPeriod,
            dataType = this.dataType,
            userId = UUID.fromString(this.userId),
            creationTimestamp = this.creationTimestamp,
            memberComment = this.memberComment,
            adminComment = this.adminComment,
            lastModifiedDate = this.lastModifiedDate,
            requestPriority = this.requestPriority,
            state = this.state,
            dataSourcingEntity = null,
        )
}
