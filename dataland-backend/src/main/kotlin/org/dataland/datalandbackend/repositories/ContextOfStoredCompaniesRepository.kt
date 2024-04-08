package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the StoredCompany Entity
 */

interface ContextOfStoredCompaniesRepository : JpaRepository<StoredCompanyEntity, String> {

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

    /**
     * A function for counting the number of companies by various filters:
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * of one of the supplied dataTypes are returned
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value = "WITH" +
            " has_data AS (" +
            "SELECT DISTINCT company_id FROM data_meta_information" +
            " WHERE (:#{#searchFilter.dataTypeFilterSize} > 0" +
            " AND data_type IN :#{#searchFilter.dataTypeFilter} AND quality_status = 1) " +
            " UNION SELECT DISTINCT company_id FROM stored_companies WHERE :#{#searchFilter.dataTypeFilterSize} = 0)," +
            " filtered_results AS (" +
            " SELECT intermediate_results.company_id AS company_id, min(intermediate_results.match_quality)" +
            " AS match_quality FROM (" +
            " (SELECT company.company_id AS company_id," +
            " CASE " +
            " WHEN company_name = :#{#searchFilter.searchString} THEN 1" +
            " WHEN company_name ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 3" +
            " ELSE 5" +
            " END match_quality " +
            " FROM (SELECT company_id, company_name FROM stored_companies) company " +
            " JOIN has_data datainfo" +
            " ON company.company_id = datainfo.company_id " +
            " WHERE company.company_name ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()})" +

            " UNION " +
            " (SELECT " +
            " stored_company_entity_company_id AS company_id," +
            " CASE " +
            " WHEN company_alternative_names = :#{#searchFilter.searchString} THEN 2" +
            " WHEN company_alternative_names" +
            " ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 4" +
            " ELSE 5 " +
            " END match_quality " +
            " FROM stored_company_entity_company_alternative_names alt_names" +
            " JOIN has_data datainfo" +
            " ON alt_names.stored_company_entity_company_id = datainfo.company_id " +
            " WHERE company_alternative_names" +
            " ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()})" +

            " UNION " +
            " (SELECT " +
            " identifiers.company_id AS company_id," +
            " 5 match_quality " +
            " FROM company_identifiers identifiers" +
            " JOIN has_data datainfo" +
            " ON identifiers.company_id = datainfo.company_id " +
            " WHERE identifier_value ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()})) " +
            " AS intermediate_results GROUP BY intermediate_results.company_id)" +

            // Combine Results
            " SELECT COUNT(*)" +
            " FROM filtered_results " +
            " JOIN " +
            " (SELECT company_id FROM stored_companies " +
            " WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0" +
            " OR country_code IN :#{#searchFilter.countryCodeFilter}) " +
            " ) info " +
            " ON info.company_id = filtered_results.company_id",
    )
    fun getNumberOfCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
    ): Int
}
