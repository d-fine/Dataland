package org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Request body for posting a non-sourceability QA decision.
 */
data class NonSourceabilityDecisionBody(
    val qaStatus: QaStatus,
    val qaComment: String?,
)
