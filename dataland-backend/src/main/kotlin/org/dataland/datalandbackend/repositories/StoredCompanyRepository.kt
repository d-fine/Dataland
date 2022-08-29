package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    @Query("SELECT company FROM StoredCompanyEntity company " +
            "LEFT JOIN company.dataRegisteredByDataland data " +
            "LEFT JOIN company.identifiers identifier " +
            "LEFT JOIN company.indices stockIndex " +
            "WHERE" +
            "(:#{#searchFilter.dataTypeFilterSize} = 0 OR (data.dataType in :#{#searchFilter.dataTypeFilter})) AND" +
            "(:#{#searchFilter.stockIndexFilterSize} = 0 OR (stockIndex in :#{#searchFilter.stockIndexFilter})) AND " +
            "(:#{#searchFilter.searchStringLength} = 0 OR (lower(company.companyName) LIKE %:#{#searchFilter.searchStringLower}%) or " +
            "(:#{#searchFilter.nameOnlyFilter} = false AND lower(identifier.identifierValue) LIKE %:#{#searchFilter.searchStringLower}%)) " +
            "GROUP BY company.companyId " +
            "ORDER BY (CASE WHEN lower(company.companyName) = :#{#searchFilter.searchStringLower} THEN 1 WHEN lower(company.companyName) LIKE :#{#searchFilter.searchStringLower}% THEN 2 ELSE 3 END), company.companyName")
    fun searchCompanies(@Param("searchFilter") searchFilter: StoredCompanySearchFilter): List<StoredCompanyEntity>
}