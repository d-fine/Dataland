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
                "child_company_id",
                "parent_company_id",
                "framework",
                "reporting_period",
            ],
        ),
    ],
)
@Suppress("LongParameterList")
class CompanyProxyEntity(
    @Id
    @Column(name = "id", nullable = false)
    val proxyId: UUID = UUID.randomUUID(),
    @Column(name = "child_company_id", nullable = false)
    var childCompanyId: UUID,
    @Column(name = "parent_company_id", nullable = false)
    var parentCompanyId: UUID,
    @Column(name = "framework")
    var framework: String? = null, // null => all frameworks
    @Column(name = "reporting_period")
    var reportingPeriod: String? = null, // null => all periods
    @Column(name = "upload_timestamp", nullable = false)
    var uploadTimestamp: Long,
    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,
) {
    /**
     * Converts this CompanyProxyEntity to a StoredCompanyProxy.
     */
    fun toStoredCompanyProxy(): StoredCompanyProxy =
        StoredCompanyProxy(
            proxyId = proxyId.toString(),
            childCompanyId = childCompanyId.toString(),
            parentCompanyId = parentCompanyId.toString(),
            framework = framework,
            reportingPeriod = reportingPeriod,
            uploadTimestamp = uploadTimestamp,
            uploaderUserId = uploaderUserId,
        )
}
