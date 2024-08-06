package org.dataland.datalandqaservice.utils

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.UserAuthenticatedApiService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * A QaReportSecurityPolicy that disables backend data-existence and access checks
 * for QA reports for testing.
 */
class NoBackendRequestQaReportSecurityPolicy(
    userAuthenticatedApiService: UserAuthenticatedApiService,
) : QaReportSecurityPolicy(userAuthenticatedApiService) {
    override fun ensureUserCanViewQaReportForDataId(dataId: String, user: DatalandAuthentication) {
        // Do nothing. This function is checked in an E2E-test.
    }
}
