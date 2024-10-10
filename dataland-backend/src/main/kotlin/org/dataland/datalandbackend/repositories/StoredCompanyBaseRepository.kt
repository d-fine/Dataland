package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.repositories.utils.TemporaryTables
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the StoredCompany Entity
 */
interface StoredCompanyBaseRepository : JpaRepository<StoredCompanyEntity, String> {
    /**
     * A function for counting the number of companies by various filters:
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * - countryFilter: If set, only companies with at least one datapoint
     * - sectorFilter: If set, only companies with at least one datapoint
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
            " SELECT COUNT(*) " +
                " FROM " + TemporaryTables.TABLE_FILTERED_TEXT_RESULTS +
                " INNER JOIN " + TemporaryTables.TABLE_FILTERED_DROPDOWN_RESULTS +
                " ON filtered_text_results.company_id = filtered_dropdown_results.company_id ",
    )
    fun getNumberOfCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
    ): Int

    /**
     * A function for counting the number of companies by various filters (excluding searchString):
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * - countryFilter: If set, only companies with at least one datapoint
     * - sectorFilter: If set, only companies with at least one datapoint
     * of one of the supplied dataTypes are returned
     */
    @Query(
        nativeQuery = true,
        value =
            " SELECT COUNT(*)" +
                " FROM " + TemporaryTables.TABLE_FILTERED_DROPDOWN_RESULTS,
    )
    fun getNumberOfCompaniesWithoutSearchString(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
    ): Int

    /**
     * Returns all available distinct country codes
     */
    @Query(
        "SELECT DISTINCT company.countryCode FROM StoredCompanyEntity company " +
            "INNER JOIN company.dataRegisteredByDataland data ",
    )
    fun fetchDistinctCountryCodes(): Set<String>

    /**
     * Returns all available distinct sectors
     */
    @Query(
        "SELECT DISTINCT company.sector FROM StoredCompanyEntity company " +
            "INNER JOIN company.dataRegisteredByDataland data " +
            "WHERE company.sector IS NOT NULL ",
    )
    fun fetchDistinctSectors(): Set<String>
}
