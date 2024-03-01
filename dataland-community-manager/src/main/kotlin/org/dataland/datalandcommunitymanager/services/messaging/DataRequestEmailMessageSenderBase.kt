package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication

/**
 * A class that provided utility for generating emails messages for data requests
 */
open class DataRequestEmailMessageSenderBase {
    protected fun buildUserInfo(
        userAuthentication: DatalandJwtAuthentication,
    ): String {
        return "User ${userAuthentication.username} (Keycloak ID: ${userAuthentication.userId})"
    }

    protected fun formatReportingPeriods(reportingPeriods: Set<String>) =
        reportingPeriods.toList().sorted().joinToString(", ")
}
