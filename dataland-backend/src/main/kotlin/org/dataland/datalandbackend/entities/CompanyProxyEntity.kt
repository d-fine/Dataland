package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import java.util.UUID

/**
 * Entity representing a company proxy relation in the database.
 */
@Entity
@Table(
    name = "company_proxy_relations",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = [
                "proxied_company_id",
                "proxy_company_id",
                "framework",
                "reporting_period",
            ],
        ),
    ],
)
class CompanyProxyEntity(
    @Id
    @Column(name = "id", nullable = false)
    val proxyId: UUID = UUID.randomUUID(),
    @Column(name = "proxied_company_id", nullable = false)
    var proxiedCompanyId: UUID,
    @Column(name = "proxy_company_id", nullable = false)
    var proxyCompanyId: UUID,
    @Column(name = "framework")
    var framework: String? = null, // null => all frameworks
    @Column(name = "reporting_period")
    var reportingPeriod: String? = null, // null => all periods
) {
    /**
     * Converts this CompanyProxyEntity to a StoredCompanyProxy.
     */
    fun toStoredCompanyProxy(): StoredCompanyProxy =
        StoredCompanyProxy(
            proxyId = proxyId.toString(),
            proxiedCompanyId = proxiedCompanyId.toString(),
            proxyCompanyId = proxyCompanyId.toString(),
            framework = framework,
            reportingPeriod = reportingPeriod,
        )
}
