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
    /**
     * A function for querying basic information for all companies with approved datasets
     */
    @Query(
        nativeQuery = true,
        value = "SELECT company.company_id as companyId, " +
            "company.company_name as companyName, " +
            "company.headquarters as headquarters, " +
            "company.country_code as countryCode, " +
            "company.sector as sector, " +
            "permId.min_id as permId " +
            "FROM stored_companies company " +
            "JOIN (SELECT distinct company_id from data_meta_information where quality_status = 1) datainfo " +
            "ON company.company_id = datainfo.company_id " +
            "LEFT JOIN (SELECT company_id, min(identifier_value) as min_id from company_identifiers where identifier_type = 'PermId' group by company_id) permId " +
            "ON company.company_id = permid.company_id " +
            "ORDER by company.company_name asc "
    )
    fun getAllCompaniesWithDataset(): List<BasicCompanyInformation>

    /**
     * A function for querying basic information of companies with approved datasets by various filters:
     * - dataTypeFilter: If set, only companies with at least one datapoint
     * of one of the supplied dataTypes are returned
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value = "WITH " +
            " has_data as (SELECT distinct company_id from data_meta_information where (:#{#searchFilter.dataTypeFilterSize} = 0 OR data_type in :#{#searchFilter.dataTypeFilter}) and quality_status = 1), " +
            " filtered_results as (" +
            " SELECT intermediate_results.company_id as company_id, min(intermediate_results.match_quality) as match_quality from (" +
            " (SELECT company.company_id as company_id," +
            " CASE " +
            " WHEN company_name = :#{#searchFilter.searchString} THEN 1" +
            " WHEN company_name ILIKE :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()} THEN 3" +
            " ELSE 5" +
            " END match_quality " +
            " FROM (SELECT company_id, company_name FROM stored_companies) company " +
            " JOIN has_data datainfo" +
            " ON company.company_id = datainfo.company_id " +
            " WHERE company.company_name ILIKE '%' || :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()})" +

            " UNION " +
            " (SELECT " +
            " stored_company_entity_company_id AS company_id," +
            " CASE " +
            " WHEN company_alternative_names = :#{#searchFilter.searchString} THEN 2" +
            " WHEN company_alternative_names ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 4" +
            " ELSE 5 " +
            " END match_quality " +
            " FROM stored_company_entity_company_alternative_names alt_names" +
            " JOIN has_data datainfo" +
            " ON alt_names.stored_company_entity_company_id = datainfo.company_id " +
            " WHERE company_alternative_names ILIKE '%' || :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()})" +

            " UNION " +
            " (SELECT " +
            " identifiers.company_id as company_id," +
            " 5 match_quality " +
            " FROM company_identifiers identifiers" +
            " JOIN has_data datainfo" +
            " ON identifiers.company_id = datainfo.company_id " +
            " WHERE identifier_value ILIKE '%' || :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()})) " +
            " as intermediate_results group by intermediate_results.company_id) " +

            // Combine Results
            " SELECT info.company_id AS companyId," +
            " info.company_name AS companyName, " +
            " info.headquarters as headquarters, " +
            " info.country_code as countryCode, " +
            " info.sector as sector, " +
            " perm_id.identifier_value AS permId " +
            " FROM filtered_results " +
            " JOIN " +
            " (select company_id, company_name, headquarters, country_code, sector from stored_companies " +
            " WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector in :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0 OR country_code in :#{#searchFilter.countryCodeFilter}) " +
            " ) info " +
            " ON info.company_id = filtered_results.company_id " +
            " LEFT JOIN (SELECT company_id, MIN(identifier_value) as identifier_value from company_identifiers where identifier_type = 'PermId' group by company_id) perm_id " +
            " ON perm_id.company_id = filtered_results.company_id " +
            " ORDER BY filtered_results.match_quality asc, info.company_name asc "
    )
    fun searchCompanies2(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter
    ): List<BasicCompanyInformation>

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
            "(:#{#searchFilter.uploaderIdLength} = 0 " +
            "OR (data.uploaderUserId = :#{#searchFilter.uploaderId})) AND " +
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
            "company.companyName ASC",
    )
    fun searchCompanies(@Param("searchFilter") searchFilter: StoredCompanySearchFilter): List<StoredCompanyEntity>

    /**
     * A function for querying companies by search string:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
        "WITH filtered_text_results as (" +
            // Fuzzy-Search Company Name
            " (SELECT stored_companies.company_id, max(stored_companies.company_name) as company_name," +
            " max(CASE " +
            " WHEN company_name = :#{#searchString} THEN 10" +
            " WHEN company_name ILIKE :#{escape(#searchString)}% ESCAPE :#{escapeCharacter()} THEN 5" +
            " ELSE 1" +
            " END) match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM stored_companies" +
            " LEFT JOIN data_meta_information " +
            " ON stored_companies.company_id = data_meta_information.company_id AND currently_active = true" +
            " WHERE company_name ILIKE %:#{escape(#searchString)}% ESCAPE :#{escapeCharacter()}" +
            " GROUP BY stored_companies.company_id" +
            " ORDER BY" +
            " dataset_rank DESC," +
            " match_quality DESC, stored_companies.company_id LIMIT :#{#resultLimit})" +

            " UNION " +
            // Fuzzy-Search Company Alternative Name
            " (SELECT " +
            " stored_company_entity_company_id AS company_id," +
            " max(stored_companies.company_name) AS company_name," +
            " max(CASE " +
            " WHEN company_alternative_names = :#{#searchString} THEN 9" +
            " WHEN company_alternative_names ILIKE :#{escape(#searchString)}% ESCAPE :#{escapeCharacter()} THEN 4" +
            " ELSE 1 " +
            " END) match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM stored_company_entity_company_alternative_names" +
            " JOIN stored_companies ON stored_companies.company_id = " +
            " stored_company_entity_company_alternative_names.stored_company_entity_company_id  " +
            " LEFT JOIN data_meta_information " +
            " ON stored_company_entity_company_id = data_meta_information.company_id AND currently_active = true" +
            " WHERE company_alternative_names ILIKE %:#{escape(#searchString)}% ESCAPE :#{escapeCharacter()}" +
            " GROUP BY stored_company_entity_company_id" +
            " ORDER BY " +
            " dataset_rank DESC," +
            " match_quality DESC, stored_company_entity_company_id LIMIT :#{#resultLimit})" +

            " UNION" +
            // Fuzzy-Search Company Identifier
            " (SELECT company_identifiers.company_id, max(stored_companies.company_name) AS company_name," +
            " max(CASE " +
            " WHEN identifier_value = :#{#searchString} THEN 10" +
            " WHEN identifier_value ILIKE :#{escape(#searchString)}% ESCAPE :#{escapeCharacter()} THEN 3" +
            " ELSE 0" +
            " END) AS match_quality, " +
            " max(CASE WHEN data_id IS NOT null THEN 2 else 1 END) AS dataset_rank" +
            " FROM company_identifiers" +
            " JOIN stored_companies ON stored_companies.company_id = company_identifiers.company_id " +
            " LEFT JOIN data_meta_information " +
            " ON company_identifiers.company_id = data_meta_information.company_id AND currently_active = true" +
            " WHERE identifier_value ILIKE %:#{escape(#searchString)}% ESCAPE :#{escapeCharacter()} " +
            " GROUP BY company_identifiers.company_id" +
            " ORDER BY " +
            " dataset_rank DESC," +
            " match_quality DESC, company_identifiers.company_id LIMIT :#{#resultLimit})) " +
            // Combine Results
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
     * Used for pre-fetching the identifiers field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.identifiers WHERE company in :companies",
    )
    fun fetchIdentifiers(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the alternative company names field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.companyAlternativeNames WHERE company in :companies",
    )
    fun fetchAlternativeNames(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Used for pre-fetching the dataStoredByDataland field of a list of stored companies
     */
    @Query(
        "SELECT DISTINCT company FROM StoredCompanyEntity company " +
            "LEFT JOIN FETCH company.dataRegisteredByDataland WHERE company in :companies",
    )
    fun fetchCompanyAssociatedByDataland(companies: List<StoredCompanyEntity>): List<StoredCompanyEntity>

    /**
     * Retrieves all the teaser companies
     */
    fun getAllByIsTeaserCompanyIsTrue(): List<StoredCompanyEntity>
}
