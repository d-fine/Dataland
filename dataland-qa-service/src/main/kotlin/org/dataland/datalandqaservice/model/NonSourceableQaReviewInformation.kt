package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * API model for non-sourceable QA review information.
 */
data class NonSourceableQaReviewInformation(
    @field:Schema(
        description = "Unique identifier of the QA review item.",
    )
    val id: UUID?,
    @field:Schema(
        description = "Identifier of the non-sourceability request in the backend service.",
    )
    val nonSourceabilityId: UUID,
    @field:Schema(
        description = "Identifier of the company this request belongs to.",
    )
    val companyId: String,
    @field:Schema(
        description = "Data type this request belongs to.",
    )
    val dataType: String,
    @field:Schema(
        description = "Reporting period this request belongs to.",
    )
    val reportingPeriod: String,
    @field:Schema(
        description = "Reason given for the non-sourceability claim.",
    )
    val reason: String?,
    @field:Schema(
        description = "Uploader user ID from the originating request.",
    )
    val uploaderUserId: String,
    @field:Schema(
        description = "Upload time from the originating request.",
    )
    val uploadTime: Long,
    @field:Schema(
        description = "Current QA status of the review item.",
    )
    val qaStatus: QaStatus,
    @field:Schema(
        description = "Reviewer user ID for final decisions.",
    )
    val reviewerUserId: String?,
    @field:Schema(
        description = "Optional reviewer comment.",
    )
    val qaComment: String?,
)
