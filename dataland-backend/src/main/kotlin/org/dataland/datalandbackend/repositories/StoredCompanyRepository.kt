package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the StoredCompany Entity
 */

interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    companion object {
        const val TABLE_LEIS = " (" +
            // get all LEI identifiers
            "SELECT identifier_value, company_id FROM company_identifiers " +
            " WHERE identifier_type='Lei'" +
            ") AS leis "

        const val TABLE_FILTERED_TEXT_RESULTS = " filtered_text_results AS (" +
            "(SELECT stored_companies.company_id, max(stored_companies.company_name) AS company_name," +
            " max(CASE " +
            " WHEN company_name = :#{#searchFilter.searchString} THEN 10" +
            " WHEN company_name ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 5" +
            " ELSE 1" +
            " END) match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM stored_companies" +
            " LEFT JOIN data_meta_information " +
            " ON stored_companies.company_id = data_meta_information.company_id AND currently_active = true" +
            " WHERE company_name ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()}" +
            " GROUP BY stored_companies.company_id" +
            " ) " +

            " UNION " +
            // Fuzzy-Search Company Alternative Name
            " (SELECT " +
            " stored_company_entity_company_id AS company_id," +
            " max(stored_companies.company_name) AS company_name," +
            " max(CASE " +
            " WHEN company_alternative_names = :#{#searchFilter.searchString} THEN 9 " +
            "WHEN company_alternative_names ILIKE :#{escape(#searchFilter.searchString)}% " +
            " ESCAPE :#{escapeCharacter()} THEN 4" +
            " ELSE 1 " +
            " END) match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM stored_company_entity_company_alternative_names" +
            " JOIN stored_companies ON stored_companies.company_id = " +
            " stored_company_entity_company_alternative_names.stored_company_entity_company_id  " +
            " LEFT JOIN data_meta_information " +
            " ON stored_company_entity_company_id = data_meta_information.company_id AND currently_active = true " +
            "WHERE " +
            " company_alternative_names ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()}" +
            " GROUP BY stored_company_entity_company_id" +
            ") " +

            " UNION" +
            // Fuzzy-Search Company Identifier
            " (SELECT company_identifiers.company_id, max(stored_companies.company_name) AS company_name," +
            " max(CASE " +
            " WHEN identifier_value = :#{#searchFilter.searchString} THEN 10" +
            " WHEN identifier_value ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 3" +
            " ELSE 0" +
            " END) AS match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM company_identifiers" +
            " JOIN stored_companies ON stored_companies.company_id = company_identifiers.company_id " +
            " LEFT JOIN data_meta_information " +
            " ON company_identifiers.company_id = data_meta_information.company_id AND currently_active = true" +
            " WHERE identifier_value ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} " +
            " GROUP BY company_identifiers.company_id)" +
            " ) "
        const val TABLE_FILTERED_DROPDOWN_RESULTS = "filtered_dropdown_results AS (" +
            " SELECT company_id FROM stored_companies " +
            " WHERE (company_id IN " +
            "(SELECT DISTINCT company_id FROM data_meta_information WHERE currently_active='true'" +
            " AND :#{#searchFilter.dataTypeFilterSize} > 0" +
            " AND data_type IN :#{#searchFilter.dataTypeFilter}) OR :#{#searchFilter.dataTypeFilterSize} = 0) " +
            " AND  (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0" +
            " OR country_code IN :#{#searchFilter.countryCodeFilter})" +
            ") "
    }

    /**
     * A function for querying basic information for all companies with approved datasets
     */
    @Query(
        nativeQuery = true,
        value = "SELECT has_active_data.company_id AS companyId," +
            " company_name AS companyName," +
            " headquarters AS headquarters, " +
            " country_code AS countryCode, " +
            " sector AS sector, " +
            " identifier_value AS lei " +
            // get required information from stored companies where active data set exists +
            " FROM (" +
            " SELECT company_id, company_name, headquarters, country_code, sector FROM stored_companies " +
            " WHERE company_id IN " +
            "(SELECT DISTINCT company_id FROM data_meta_information WHERE currently_active='true') " +

            " ORDER BY company_name ASC LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) AS has_active_data " +
            " LEFT JOIN " + TABLE_LEIS +
            " ON leis.company_id=has_active_data.company_Id" +
            " ORDER BY company_name ASC",
    )
    fun getAllCompaniesWithDataset(
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<BasicCompanyInformation>

    /**
     * A function for querying basic information for all companies with approved datasets
     */
    @Query(
        nativeQuery = true,
        value = "WITH " +
            TABLE_FILTERED_DROPDOWN_RESULTS +
            "SELECT filtered_data.company_id AS companyId," +
            " company_name AS companyName," +
            " headquarters AS headquarters, " +
            " country_code AS countryCode, " +
            " sector AS sector, " +
            " identifier_value AS lei " +
            // get required information from stored companies with are included in the dropdown filter
            " FROM (" +
            " SELECT stored_companies.company_id, company_name, headquarters, country_code, sector " +
            " FROM stored_companies " +
            " INNER JOIN filtered_dropdown_results " +
            " ON stored_companies.company_id = filtered_dropdown_results.company_id " +
            " ORDER BY company_name ASC LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) AS filtered_data " +
            " LEFT JOIN " + TABLE_LEIS +
            " ON leis.company_id=filtered_data.company_Id" +
            " ORDER BY company_name ASC",
    )
    fun searchCompaniesWithoutSearchString(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<BasicCompanyInformation>

    /**
     * A function for querying basic information of companies by various filters:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * - country Code: If not empty, only companies with a country Code in the given set of country codes
     * - sector Code: If not empty, only companies with a sector Code in the given set of sector codes
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
        "WITH " +
            TABLE_FILTERED_TEXT_RESULTS +
            ", " +
            TABLE_FILTERED_DROPDOWN_RESULTS +
            ", " +
            " chunked_results AS (" +
            " SELECT filtered_text_results.company_id AS companyId," +
            " MIN(filtered_text_results.company_name) AS companyName," +
            " MAX(filtered_text_results.dataset_rank) AS maxDatasetRank," +
            " MAX(filtered_text_results.match_quality) AS maxMatchQuality" +
            " FROM filtered_text_results " +
            " INNER JOIN filtered_dropdown_results " +
            " ON filtered_text_results.company_id = filtered_dropdown_results.company_id " +
            " GROUP BY filtered_text_results.company_id" +
            " ORDER BY " +
            " maxDatasetRank DESC," +
            " maxMatchQuality DESC, companyName ASC " +
            " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset} )" +

            " Select companyId, companyName, " +
            " headquarters, " +
            " country_code AS countryCode, " +
            " sector, " +
            " leis.identifier_value AS lei " +
            " FROM (SELECT chunked_results.companyId, chunked_results.companyName, maxDatasetRank, maxMatchQuality, " +
            " headquarters, country_code, sector From chunked_results" +
            " LEFT JOIN stored_companies ON chunked_results.companyId = stored_companies.company_id) AS filtered_data" +
            " LEFT JOIN " + TABLE_LEIS +
            "ON leis.company_id=filtered_data.companyId " +
            "ORDER BY maxDatasetRank DESC, maxMatchQuality DESC, companyName ASC",
    )
    fun searchCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<BasicCompanyInformation>

    /**
     * A function for querying companies by search string:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
        "WITH " + TABLE_FILTERED_TEXT_RESULTS +

            " SELECT filtered_text_results.company_id AS companyId," +
            " MIN(filtered_text_results.company_name) AS companyName" +
            " FROM filtered_text_results " +
            " LEFT JOIN data_meta_information " +
            " ON filtered_text_results.company_id = data_meta_information.company_id AND currently_active = true" +
            " GROUP BY filtered_text_results.company_id" +
            " ORDER BY " +
            " max(dataset_rank) DESC," +
            " MAX(filtered_text_results.match_quality) DESC, companyId " +
            " LIMIT :#{#resultLimit}",
    )
    fun searchCompaniesByNameOrIdentifier(
        @Param("searchString") searchString: String,
        @Param("resultLimit") resultLimit: Int = 100,
    ): List<CompanyIdAndName>

    /**
     * Used for pre-fetching the identifiers field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.identifiers WHERE company IN :companies",
    )
    fun fetchIdentifiers(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the alternative company names field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyAlternativeNames WHERE company IN :companies",
    )
    fun fetchAlternativeNames(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the dataStoredByDataland field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.dataRegisteredByDataland WHERE company IN :companies",
    )
    fun fetchCompanyAssociatedByDataland(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Retrieves all the teaser companies
     */
    fun getAllByIsTeaserCompanyIsTrue(): List<StoredCompanyEntity>

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
        value = "WITH " +
            TABLE_FILTERED_TEXT_RESULTS +
            ", " +
            TABLE_FILTERED_DROPDOWN_RESULTS +

            // Count Results
            " SELECT COUNT(*)" +
            " FROM filtered_text_results INNER JOIN filtered_dropdown_results " +
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
        value = "WITH " +
            TABLE_FILTERED_DROPDOWN_RESULTS +

            " SELECT COUNT(*)" +
            " FROM filtered_dropdown_results ",
    )
    fun getNumberOfCompaniesWithoutSearchString(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
    ): Int
}
