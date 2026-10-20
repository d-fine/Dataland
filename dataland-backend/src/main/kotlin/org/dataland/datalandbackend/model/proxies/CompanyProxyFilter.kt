package org.dataland.datalandbackend.model.proxies

import java.util.UUID

/**
 * Filter class for querying company proxy relations.
 *
 * @param childCompanyId The company whose data may be substituted.
 * @param parentCompanyId The company whose data may serve as a proxy.
 */
data class CompanyProxyFilter(
    val childCompanyId: UUID? = null,
    val parentCompanyId: UUID? = null,
)
