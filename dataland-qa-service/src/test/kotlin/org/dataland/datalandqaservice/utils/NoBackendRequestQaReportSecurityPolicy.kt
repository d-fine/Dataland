package org.dataland.datalandqaservice.utils

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.UserAuthenticatedBackendClient
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * A QaReportSecurityPolicy that disables backend data-existence and access checks
 * for QA reports for testing.
 */
class NoBackendRequestQaReportSecurityPolicy(
    userAuthenticatedBackendClient: UserAuthenticatedBackendClient,
) : QaReportSecurityPolicy(userAuthenticatedBackendClient) {
    override fun ensureUserCanViewQaReportForDataId(
        dataId: String,
        user: DatalandAuthentication,
    ) {
        // Do nothing. This function is checked in an E2E-test.
    }

    override fun ensureUserCanViewDataPointQaReportForDataId(
        dataId: String,
        user: DatalandAuthentication,
    ) {
        // Do nothing. This function is checked in an E2E-test.
    }
}
