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
@Suppress("TooManyFunctions")
interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String> {
    /**
     * A function for querying basic information for all companies with approved datasets
     */
    @Query(
        nativeQuery = true,
        value =
            " SELECT has_active_data.company_id AS companyId, " +
                " company_name AS companyName, " +
                " headquarters, " +
                " country_code AS countryCode, " +
                " sector, " +
                " identifier_value AS lei " +
                " FROM ( " +
                " SELECT company_id, company_name, headquarters, country_code, sector, " +
                " CASE " +
                " WHEN company_id IN :#{#highlightedCompanyIds} THEN 2 " +
                " ELSE 1 " +
                " END AS dataset_rank " +
                " FROM stored_companies " +
                " WHERE company_id IN " +
                " (SELECT DISTINCT company_id FROM data_meta_information WHERE currently_active = 'true') " +
                " ORDER BY dataset_rank DESC, company_name ASC " +
                " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) AS has_active_data " +
                " LEFT JOIN " + TemporaryTables.TABLE_LEIS +
                " ON leis.company_id = has_active_data.company_id",
    )
    fun getAllCompaniesWithDataset(
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
        @Param("highlightedCompanyIds") highlightedCompanyIds: List<String>,
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
     * Used for pre-fetching the identifiers field of a single stored company. ISINs are no
     * longer stored in the table company_identifiers, hence the name of the method.
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.identifiers identifier " +
            "WHERE company = :storedCompany AND (identifier.identifierType != 'Isin' OR identifier IS NULL)",
    )
    fun fetchNonIsinIdentifiers(storedCompany: StoredCompanyEntity): StoredCompanyEntity

    /**
     * Used for pre-fetching the alternative company names field of a single stored company
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyAlternativeNames WHERE company = :storedCompany",
    )
    fun fetchAlternativeNames(storedCompany: StoredCompanyEntity): StoredCompanyEntity

    /**
     * Used for pre-fetching the company contact details field of a single stored company
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyContactDetails WHERE company = :storedCompany",
    )
    fun fetchCompanyContactDetails(storedCompany: StoredCompanyEntity): StoredCompanyEntity

    /**
     * Used for pre-fetching the email suffixes field of a single stored company
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.emailSuffixes WHERE company = :storedCompany",
    )
    fun fetchEmailSuffixes(storedCompany: StoredCompanyEntity): StoredCompanyEntity

    /**
     * Used for pre-fetching the dataRegisteredByDataland field of a single stored company
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.dataRegisteredByDataland WHERE company = :storedCompany",
    )
    fun fetchCompanyAssociatedByDataland(storedCompany: StoredCompanyEntity): StoredCompanyEntity

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
                "childCompanies.company_id AS companyId, " +
                "childCompanies.company_name AS companyName, " +
                "childCompanies.headquarters, " +
                "childCompanies.country_code AS countryCode, " +
                "childCompanies.sector, " +
                "childIdentifiers.identifier_value AS lei " +
                "FROM stored_companies childCompanies " +
                "INNER JOIN company_identifiers parentIdentifiers " +
                "ON parentIdentifiers.identifier_value = childCompanies.parent_company_lei " +
                "LEFT JOIN company_identifiers childIdentifiers " +
                "ON childIdentifiers.company_id = childCompanies.company_id " +
                "AND childIdentifiers.identifier_type = 'Lei'" +
                "WHERE parentIdentifiers.identifier_type = 'Lei' " +
                "AND parentIdentifiers.company_id = :#{#companyId};",
    )
    fun getCompanySubsidiariesByParentId(
        @Param("companyId") companyId: String,
    ): List<BasicCompanyInformation>

    /**
     * Finds a `StoredCompanyEntity` based on the identifier value (always a lei)
     *
     * @param leis The value of the identifier to search for.
     * @return A List<StoredCompanyEntity> matching the given list of leis, or `null` if no match is found.
     */
    @Query
    (
        "SELECT company FROM  StoredCompanyEntity company " +
            "JOIN FETCH company.identifiers companyIdentifierEntity " +
            "WHERE companyIdentifierEntity.identifierValue in :leis ",
    )
    fun findCompaniesbyListOfLeis(
        @Param("leis") leis: List<String>,
    ): List<StoredCompanyEntity>?
}
