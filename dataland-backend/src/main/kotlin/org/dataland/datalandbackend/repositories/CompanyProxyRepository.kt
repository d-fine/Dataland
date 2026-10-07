package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxyFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository for managing CompanyProxyEntity objects.
 */
interface CompanyProxyRepository : JpaRepository<CompanyProxyEntity, UUID> {
    /**
     * Finds all CompanyProxyEntity objects with the given childCompanyId.
     *
     * @param childCompanyId The ID of the proxied company.
     * @return A list of CompanyProxyEntity objects.
     */
    fun findAllByChildCompanyId(childCompanyId: UUID): List<CompanyProxyEntity>

    /**
     * Finds all CompanyProxyEntity objects matching the given filters.
     *
     * @param companyProxyFilter The filter criteria.
     * @param pageable The pagination information.
     * @return A page of CompanyProxyEntity objects matching the filters.
     */
    @Query(
        """
    SELECT c FROM CompanyProxyEntity c
    WHERE
        (:#{#searchFilter.childCompanyId} IS NULL OR c.childCompanyId = :#{#searchFilter.childCompanyId})
        AND (:#{#searchFilter.parentCompanyId} IS NULL OR c.parentCompanyId = :#{#searchFilter.parentCompanyId})
        AND (
            (:#{#searchFilter.frameworks.isEmpty()} = TRUE)
            OR (c.framework IN :#{#searchFilter.frameworks})
        )
        AND (
            (:#{#searchFilter.reportingPeriods.isEmpty()} = TRUE)
            OR (c.reportingPeriod IN :#{#searchFilter.reportingPeriods})
        )
    """,
    )
    fun findByFilters(
        @Param("searchFilter") companyProxyFilter: CompanyProxyFilter?,
        pageable: Pageable,
    ): Page<CompanyProxyEntity>
}
