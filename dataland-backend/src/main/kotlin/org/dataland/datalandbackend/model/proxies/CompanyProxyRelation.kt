package org.dataland.datalandbackend.model.proxies

import java.util.UUID

// Represents a single row in company_proxy_relations
data class CompanyProxyRule(
    val proxiedCompanyId: UUID,
    val proxyCompanyId: UUID,
    val framework: String?, // null == "all frameworks"
    val reportingPeriod: String?, // null == "all periods"
)
