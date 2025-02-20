package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.repositories.utils.TemporaryTables
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the StoredCompany Entity
 */

interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    /**
     * A function for querying basic information for all companies with approved datasets
     */
    @Query(
        nativeQuery = true,
        value =
            " SELECT has_active_data.company_id AS companyId," +
                " company_name AS companyName," +
                " headquarters, " +
                " country_code AS countryCode, " +
                " sector, " +
                " identifier_value AS lei " +
                // get required information from stored companies where active dataset exists
                " FROM (" +
                " SELECT company_id, company_name, headquarters, country_code, sector FROM stored_companies " +
                " WHERE company_id IN " +
                " (SELECT DISTINCT company_id FROM data_meta_information WHERE currently_active = 'true') " +
                " ORDER BY company_name ASC LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) AS has_active_data " +
                " LEFT JOIN " + TemporaryTables.TABLE_LEIS +
                " ON leis.company_id = has_active_data.company_Id" +
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
        value =
            " SELECT filtered_data.company_id AS companyId," +
                " company_name AS companyName," +
                " headquarters, " +
                " country_code AS countryCode, " +
                " sector, " +
                " identifier_value AS lei " +
                // get required information from stored companies which are included in the dropdown filter
                " FROM (" +
                " SELECT stored_companies.company_id, company_name, headquarters, country_code, sector " +
                " FROM stored_companies " +
                " INNER JOIN " + TemporaryTables.TABLE_FILTERED_DROPDOWN_RESULTS +
                " ON stored_companies.company_id = filtered_dropdown_results.company_id " +
                " ORDER BY company_name ASC LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) AS filtered_data " +
                " LEFT JOIN " + TemporaryTables.TABLE_LEIS +
                " ON leis.company_id = filtered_data.company_Id" +
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
            " WITH " +
                " chunked_results AS (" +
                " SELECT filtered_text_results.company_id AS companyId," +
                " MIN(filtered_text_results.company_name) AS companyName," +
                " MAX(filtered_text_results.dataset_rank) AS maxDatasetRank," +
                " MAX(filtered_text_results.match_quality) AS maxMatchQuality" +
                " FROM " + TemporaryTables.TABLE_FILTERED_TEXT_RESULTS +
                " INNER JOIN " + TemporaryTables.TABLE_FILTERED_DROPDOWN_RESULTS +
                " ON filtered_text_results.company_id = filtered_dropdown_results.company_id " +
                " GROUP BY filtered_text_results.company_id" +
                " ORDER BY maxDatasetRank DESC, maxMatchQuality DESC, companyName ASC " +
                " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset})" +

                " SELECT companyId," +
                " companyName, " +
                " headquarters, " +
                " country_code AS countryCode, " +
                " sector, " +
                " leis.identifier_value AS lei " +
                " FROM (SELECT chunked_results.companyId, chunked_results.companyName, maxDatasetRank, maxMatchQuality, " +
                " headquarters, country_code, sector FROM chunked_results" +
                " LEFT JOIN stored_companies ON chunked_results.companyId = stored_companies.company_id) AS filtered_data" +
                " LEFT JOIN " + TemporaryTables.TABLE_LEIS +
                " ON leis.company_id=filtered_data.companyId " +
                " ORDER BY maxDatasetRank DESC, maxMatchQuality DESC, companyName ASC",
    )
    fun searchCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<BasicCompanyInformation>

    /**
     * A function for querying companies by search string:
     * - searchFilter.searchString: If not empty,
     *      only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
            " SELECT filtered_text_results.company_id AS companyId," +
                " MIN(filtered_text_results.company_name) AS companyName" +
                " FROM " + TemporaryTables.TABLE_FILTERED_TEXT_RESULTS +
                " LEFT JOIN data_meta_information " +
                " ON filtered_text_results.company_id = data_meta_information.company_id AND currently_active = true" +
                " GROUP BY filtered_text_results.company_id" +
                " ORDER BY MAX(dataset_rank) DESC, MAX(filtered_text_results.match_quality) DESC, companyName ASC " +
                " LIMIT :#{#resultLimit}",
    )
    fun searchCompaniesByNameOrIdentifier(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
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
     * Used for pre-fetching the company contact details field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyContactDetails WHERE company IN :companies",
    )
    fun fetchCompanyContactDetails(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

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
     * A method to retrieve a list of subsidiaries of an unltimate parent company.
     * @param companyId identifier of the ultimate parent company in dataland
     * @return list of subsidiaries
     */
    @Query(
        nativeQuery = true,
        value =
            "SELECT " +
                "CHILD_COMPANIES.company_id AS companyId, " +
                "CHILD_COMPANIES.company_name AS companyName, " +
                "CHILD_COMPANIES.headquarters, " +
                "CHILD_COMPANIES.country_code AS countryCode, " +
                "CHILD_COMPANIES.sector, " +
                "CHILD_IDENTIFIERS.identifier_value AS lei " +
                "FROM stored_companies CHILD_COMPANIES " +
                "INNER JOIN company_identifiers PARENT_IDENTIFIERS " +
                "ON PARENT_IDENTIFIERS.identifier_value = CHILD_COMPANIES.parent_company_lei " +
                "LEFT JOIN company_identifiers CHILD_IDENTIFIERS " +
                "ON CHILD_IDENTIFIERS.company_id = CHILD_COMPANIES.company_id " +
                "AND CHILD_IDENTIFIERS.identifier_type = 'Lei'" +
                "WHERE PARENT_IDENTIFIERS.identifier_type = 'Lei' " +
                "AND PARENT_IDENTIFIERS.company_id = :#{#companyId};",
    )
    fun getCompanySubsidiariesByParentId(
        @Param("companyId") companyId: String,
    ): List<BasicCompanyInformation>
}
