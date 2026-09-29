package org.dataland.datalanduserservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * Embeddable entity storing a single proxy company replacement within a portfolio's element collection.
 */
@Embeddable
data class ProxyCompanyReplacementEmbeddable(
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "timestamp")
    val timestamp: Long,
    @Column(name = "proxied_company_id")
    val proxiedCompanyId: String,
    @Column(name = "proxy_company_id")
    val proxyCompanyId: String,
)
