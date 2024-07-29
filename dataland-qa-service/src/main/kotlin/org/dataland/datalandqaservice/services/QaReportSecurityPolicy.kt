package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.stereotype.Service

/**
 * The centralized security policy for QA reports.
 */
@Service
class QaReportSecurityPolicy {
    /**
     * Checks if a user can change the active status of a report.
     * @param report the report to change the active status of
     * @param user the user requesting the change
     * @return true if the user can change the active status of the report, false otherwise
     */
    fun userCanChangeReportActiveStatus(report: QaReportEntity, user: DatalandAuthentication): Boolean {
        return report.reporterUserId == user.userId || user.roles.contains(DatalandRealmRole.ROLE_ADMIN)
    }
}
