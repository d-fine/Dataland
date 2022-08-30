package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import javax.persistence.QueryHint

/**
 * A JPA repository for accessing the StoredCompany Entity
 */
interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    /**
     * A function for querying companies by various filters:
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * of one of the supplied dataTypes are returned
     * - stockIndexFilter: If set, only companies that are listed in at least one
     * of the supplied stock indices are returned
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     * - nameOnlyFilter: If false, it suffices if the searchString is contained
     * in one of the company identifiers or the name
     */
    @Query(
        "SELECT company FROM StoredCompanyEntity company " +
            "LEFT JOIN company.dataRegisteredByDataland data " +
            "LEFT JOIN company.identifiers identifier " +
            "LEFT JOIN company.indices stockIndex " +
            "WHERE" +
            "(:#{#searchFilter.dataTypeFilterSize} = 0" +
            " OR (data.dataType in :#{#searchFilter.dataTypeFilter})) AND" +
            "(:#{#searchFilter.stockIndexFilterSize} = 0" +
            " OR (stockIndex.id.stockIndex in :#{#searchFilter.stockIndexFilter})) AND " +
            "(:#{#searchFilter.searchStringLength} = 0" +
            " OR (lower(company.companyName) LIKE %:#{#searchFilter.searchStringLower}%) or " +
            "(:#{#searchFilter.nameOnlyFilter} = false" +
            " AND lower(identifier.identifierValue) LIKE %:#{#searchFilter.searchStringLower}%)) " +
            "GROUP BY company.companyId " +
            "ORDER BY " +
            "(CASE WHEN lower(company.companyName) = :#{#searchFilter.searchStringLower} THEN 1" +
            " WHEN lower(company.companyName) LIKE :#{#searchFilter.searchStringLower}% THEN 2 ELSE 3 END) ASC," +
            " company.companyName ASC"
    )
    fun searchCompanies(@Param("searchFilter") searchFilter: StoredCompanySearchFilter): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the indices field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.indices WHERE company in :companies"
    )
    @QueryHints(QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    fun fetchStockIndices(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the identifiers field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.identifiers WHERE company in :companies"
    )
    @QueryHints(QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    fun fetchIdentifiers(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the dataStoredByDataland field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.dataRegisteredByDataland WHERE company in :companies"
    )
    @QueryHints(QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    fun fetchCompanyAssociatedByDataland(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Retrieves all the teaser companies
     */
    fun getAllByIsTeaserCompanyIsTrue(): List<StoredCompanyEntity>
}
