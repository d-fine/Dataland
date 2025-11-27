package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompanyProxyRepository : JpaRepository<CompanyProxyEntity, UUID> {
    fun findAllByProxiedCompanyId(proxiedCompanyId: UUID): List<CompanyProxyEntity>

    fun findAllByProxiedCompanyIdAndProxyCompanyIdAndFrameworkAndReportingPeriod(
        proxiedCompanyId: UUID,
        proxyCompanyId: UUID,
        framework: String?,
        reportingPeriod: String?,
    ): List<CompanyProxyEntity>

    fun deleteAllByProxiedCompanyIdAndProxyCompanyId(
        proxiedCompanyId: UUID,
        proxyCompanyId: UUID,
    )
}
