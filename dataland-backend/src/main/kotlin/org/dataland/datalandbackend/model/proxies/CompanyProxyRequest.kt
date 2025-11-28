package org.dataland.datalandbackend.model.proxies

// Request body for POST /company-data-proxy-relation
data class CompanyProxyRequest(
    val proxiedCompanyId: String,
    val proxyCompanyId: String,
    val frameworks: List<String>?,
    val reportingPeriods: List<String>?,
)
