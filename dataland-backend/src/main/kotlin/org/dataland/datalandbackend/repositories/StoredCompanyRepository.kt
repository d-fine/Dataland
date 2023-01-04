package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import jakarta.persistence.QueryHint

/**
 * A JPA repository for accessing the StoredCompany Entity
 */
interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    /**
     * A function for querying companies by various filters:
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * of one of the supplied dataTypes are returned
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
            "LEFT JOIN company.companyAlternativeNames alternativeName " +
            "WHERE " +
            "(:#{#searchFilter.dataTypeFilterSize} = 0 " +
            "OR (data.dataType in :#{#searchFilter.dataTypeFilter})) AND " +
            "(:#{#searchFilter.sectorFilterSize} = 0 " +
            "OR (company.sector in :#{#searchFilter.sectorFilter})) AND " +
            "(:#{#searchFilter.countryCodeFilterSize} = 0 " +
            "OR (company.countryCode in :#{#searchFilter.countryCodeFilter})) AND " +
            "(:#{#searchFilter.searchStringLength} = 0 " +
            "OR (lower(company.companyName) LIKE %:#{#searchFilter.searchStringLower}%) OR " +
            "(lower(alternativeName) LIKE %:#{#searchFilter.searchStringLower}%) OR " +
            "(:#{#searchFilter.nameOnlyFilter} = false " +
            "AND lower(identifier.identifierValue) LIKE %:#{#searchFilter.searchStringLower}%)) " +
            "GROUP BY company.companyId " +
            "ORDER BY " +
            "(CASE WHEN lower(company.companyName) = :#{#searchFilter.searchStringLower} THEN 1 " +
            "WHEN lower(max(alternativeName)) = :#{#searchFilter.searchStringLower} THEN 2 " +
            "WHEN lower(company.companyName) LIKE :#{#searchFilter.searchStringLower}% THEN 3 " +
            "WHEN lower(max(alternativeName)) LIKE :#{#searchFilter.searchStringLower}% THEN 4 ELSE 5 END) ASC, " +
            "company.companyName ASC"
    )
    fun searchCompanies(@Param("searchFilter") searchFilter: StoredCompanySearchFilter): List<StoredCompanyEntity>

    /**
     * Returns all available distinct country codes
     */
    @Query(
        "SELECT DISTINCT company.countryCode FROM StoredCompanyEntity company " +
            "LEFT JOIN company.dataRegisteredByDataland data " +
            "WHERE " +
            "data.dataType NOT IN ('lksg', 'sfdr', 'sme')"
    )
    fun fetchDistinctCountryCodes(): Set<String>

    /**
     * Returns all available distinct sectors
     */
    @Query(
        "SELECT DISTINCT company.sector FROM StoredCompanyEntity company " +
            "LEFT JOIN company.dataRegisteredByDataland data " +
            "WHERE " +
            "data.dataType NOT IN ('lksg', 'sfdr', 'sme')"
    )
    fun fetchDistinctSectors(): Set<String>

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
     * Used for pre-fetching the alternative company names field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyAlternativeNames WHERE company in :companies"
    )
    @QueryHints(QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    fun fetchAlternativeNames(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

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
