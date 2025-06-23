package org.dataland.datalandmessagequeueutils.messages.data

import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * The payload for a portfolio update
 */
data class PortfolioUpdatePayload(
    val portfolioId: String,
    val companyIds: Set<String>,
    val monitoredFrameworks: Set<String>,
    val reportingPeriods: Set<String>,
    val userId: String,
    val userRoles: Set<DatalandRealmRole>,
)
