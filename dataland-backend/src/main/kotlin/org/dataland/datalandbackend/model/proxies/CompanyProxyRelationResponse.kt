package org.dataland.datalandbackend.model.proxies

// Single row of what got stored in company_proxy_relations
data class CompanyProxyRelationResponse(
    val proxyId: String,
    val proxiedCompanyId: String,
    val proxyCompanyId: String,
    val framework: String?, // null => all frameworks
    val reportingPeriod: String?, // null => all periods
)
