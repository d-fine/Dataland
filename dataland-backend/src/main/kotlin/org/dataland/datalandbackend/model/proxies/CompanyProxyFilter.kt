package org.dataland.datalandbackend.model.proxies

import java.util.UUID

/**
 * Filter class for querying company proxy relations.
 *
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param frameworks The set of frameworks for which proxying is allowed.
 *        Null means no filtering on frameworks.
 *        An empty set means filtering for entries with no framework specified.
 * @param frameworksEmpty Whether to include entries with no framework specified when filtering.
 * @param reportingPeriods The set of reporting periods for which proxying is allowed.
 *        Null means no filtering on reporting periods.
 *        An empty set means filtering for entries with no reporting period specified.
 * @param reportingPeriodsEmpty Whether to include entries with no reporting period specified when filtering.
 */
data class CompanyProxyFilter(
    val proxiedCompanyId: UUID? = null,
    val proxyCompanyId: UUID? = null,
    val frameworks: Set<String>? = null,
    val frameworksEmpty: Boolean = true,
    val reportingPeriods: Set<String>? = null,
    val reportingPeriodsEmpty: Boolean = true,
)
