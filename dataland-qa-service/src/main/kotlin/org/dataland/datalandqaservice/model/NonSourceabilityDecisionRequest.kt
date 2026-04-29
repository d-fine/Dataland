package org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Request body for posting a non-sourceability QA decision.
 */
data class NonSourceabilityDecisionRequest(
    val qaStatus: QaStatus,
    val qaComment: String?,
)
