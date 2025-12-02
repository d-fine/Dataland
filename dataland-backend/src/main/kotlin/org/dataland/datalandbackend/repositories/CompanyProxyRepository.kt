package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CompanyProxyRepository : JpaRepository<CompanyProxyEntity, UUID> {
    fun findByProxyId(proxyId: UUID): CompanyProxyEntity?

    fun findAllByProxiedCompanyId(proxiedCompanyId: UUID): List<CompanyProxyEntity>

    @Query(
        """
    SELECT c FROM CompanyProxyEntity c
    WHERE
        (:proxiedCompanyId IS NULL OR c.proxiedCompanyId = :proxiedCompanyId)
        AND (:proxyCompanyId IS NULL OR c.proxyCompanyId = :proxyCompanyId)
        AND (
            (:frameworksEmpty = TRUE)
            OR (c.framework IN :frameworks)
        )
        AND (
            (:reportingPeriodsEmpty = TRUE)
            OR (c.reportingPeriod IN :reportingPeriods)
        )
    """,
    )
    fun findByFilters(
        @Param("proxiedCompanyId") proxiedCompanyId: UUID?,
        @Param("proxyCompanyId") proxyCompanyId: UUID?,
        @Param("frameworks") frameworks: Set<String>,
        @Param("frameworksEmpty") frameworksEmpty: Boolean,
        @Param("reportingPeriods") reportingPeriods: Set<String>,
        @Param("reportingPeriodsEmpty") reportingPeriodsEmpty: Boolean,
        pageable: Pageable,
    ): Page<CompanyProxyEntity>

    fun deleteAllByProxiedCompanyIdAndProxyCompanyId(
        proxiedCompanyId: UUID,
        proxyCompanyId: UUID,
    )
}
