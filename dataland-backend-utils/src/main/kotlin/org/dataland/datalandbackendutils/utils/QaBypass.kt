package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Utility object to get the comment and status for a QA bypass.
 */
object QaBypass {
    /**
     * Get the comment and status for a QA bypass.
     */
    fun getCommentAndStatusForBypass(bypassQa: Boolean): Pair<QaStatus, String?> {
        return when (bypassQa) {
                true -> Pair(QaStatus.Accepted, "Automatically QA approved.")
                false -> Pair(QaStatus.Pending, null)
            }
    }
}