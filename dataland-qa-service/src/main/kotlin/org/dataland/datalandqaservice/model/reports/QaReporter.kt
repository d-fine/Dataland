package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Embeddable
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import java.util.UUID

/**
 * Embeddable representing a reporter that submitted QA reports for a dataset under review.
 */
@Embeddable
data class QaReporter(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_ID_EXAMPLE,
    )
    val reporterUserId: UUID,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USERNAME_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USERNAME_EXAMPLE,
    )
    val reporterUserName: String?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_EMAIL_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_EMAIL_EXAMPLE,
    )
    val reporterEmailAddress: String?,
)
