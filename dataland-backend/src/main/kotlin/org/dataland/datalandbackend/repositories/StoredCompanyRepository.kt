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
        value = "SELECT has_active_data.company_id,"+
                " company_name AS companyName," +
                " headquarters AS headquarters, " +
                " country_code AS countryCode, " +
                " sector AS sector, " +
                " identifier_value AS lei " +
                //get required information from stored companies where active data set exists +
                " FROM (" +
                " SELECT company_id, company_name, headquarters, country_code, sector FROM public.stored_companies " +
                " WHERE company_id IN " +
                "(SELECT DISTINCT company_id FROM public.data_meta_information WHERE currently_active='true') " +
                // get all unique company IDs that have active data
                ") AS has_active_data" +
                " LEFT JOIN (" +
                //get all LEI identifiers
                "SELECT identifier_value, company_id FROM public.company_identifiers " +
                " WHERE identifier_type='Lei'" +
                ") AS leis_table " +
                " ON leis_table.company_id=has_active_data.company_id" +
                " ORDER BY company_name ASC" +
            " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun getAllCompaniesWithDataset(
        @Param("resultLimit") resultLimit: Int = 100,
        @Param("resultOffset") resultOffset: Int = 0,
    ): List<BasicCompanyInformation>


    /**
     * A function for querying basic information of companies with dataset(s) by various filters:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * - country Code: If not empty, only companies with a country Code in the given set of country codes
     * - sector Code: If not empty, only companies with a sector Code in the given set of sector codes
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value = "WITH" +
            " has_data AS (SELECT DISTINCT company_id FROM data_meta_information" +
            " WHERE (:#{#searchFilter.dataTypeFilterSize} = 0" +
            " OR data_type IN :#{#searchFilter.dataTypeFilter}) AND quality_status = 1" +
            ")," +
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
            " AS intermediate_results GROUP BY intermediate_results.company_id) " +

            // Combine Results
            " SELECT info.company_id AS companyId," +
            " info.company_name AS companyName, " +
            " info.headquarters AS headquarters, " +
            " info.country_code AS countryCode, " +
            " info.sector AS sector, " +
            " lei.identifier_value AS lei " +
            " FROM filtered_results " +
            " JOIN " +
            " (SELECT company_id, company_name, headquarters, country_code, sector FROM stored_companies " +
            " WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0" +
            " OR country_code IN :#{#searchFilter.countryCodeFilter}) " +
            " ) info " +
            " ON info.company_id = filtered_results.company_id " +
            " LEFT JOIN (SELECT company_id, MIN(identifier_value) AS identifier_value FROM company_identifiers" +
            " WHERE identifier_type = 'Lei' GROUP BY company_id) lei " +
            " ON lei.company_id = filtered_results.company_id " +
            " ORDER BY filtered_results.match_quality ASC, info.company_name ASC " +
            " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun searchCompaniesWithDataset(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
        @Param("resultLimit") resultLimit: Int = 100,
        @Param("resultOffset") resultOffset: Int = 0,
    ): List<BasicCompanyInformation>

    /**
     * A function for querying basic information of companies by various filters (excluding datatype filter):
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * - country Code: If not empty, only companies with a country Code in the given set of country codes
     * - sector Code: If not empty, only companies with a sector Code in the given set of sector codes
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value = "WITH" +
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
            " WHERE company_alternative_names" +
            " ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()})" +

            " UNION " +
            " (SELECT " +
            " identifiers.company_id AS company_id," +
            " 5 match_quality " +
            " FROM company_identifiers identifiers" +
            " WHERE identifier_value ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()})) " +
            " AS intermediate_results GROUP BY intermediate_results.company_id) " +

            // Combine Results
            " SELECT info.company_id AS companyId," +
            " info.company_name AS companyName, " +
            " info.headquarters AS headquarters, " +
            " info.country_code AS countryCode, " +
            " info.sector AS sector, " +
            " lei.identifier_value AS lei " +
            " FROM filtered_results " +
            " JOIN " +
            " (SELECT company_id, company_name, headquarters, country_code, sector FROM stored_companies " +
            " WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0" +
            " OR country_code IN :#{#searchFilter.countryCodeFilter}) " +
            " ) info " +
            " ON info.company_id = filtered_results.company_id " +
            " LEFT JOIN (SELECT company_id, MIN(identifier_value) AS identifier_value FROM company_identifiers" +
            " WHERE identifier_type = 'Lei' GROUP BY company_id) lei " +
            " ON lei.company_id = filtered_results.company_id " +
            " ORDER BY filtered_results.match_quality ASC, info.company_name ASC " +
            " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun searchCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
        @Param("resultLimit") resultLimit: Int = 100,
        @Param("resultOffset") resultOffset: Int = 0,
    ): List<BasicCompanyInformation>

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
            " has_data AS (SELECT DISTINCT company_id FROM data_meta_information" +
            " WHERE (:#{#searchFilter.dataTypeFilterSize} = 0" +
            " OR data_type IN :#{#searchFilter.dataTypeFilter}) AND quality_status = 1" +
            ")," +
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
            " AS intermediate_results GROUP BY intermediate_results.company_id) " +

            // Count Results
            " SELECT COUNT(*) " +
            " FROM filtered_results " +
            " JOIN " +
            " (SELECT company_id, company_name, headquarters, country_code, sector FROM stored_companies " +
            " WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
            " AND (:#{#searchFilter.countryCodeFilterSize} = 0" +
            " OR country_code IN :#{#searchFilter.countryCodeFilter}) " +
            " ) info " +
            " ON info.company_id = filtered_results.company_id ",
    )
    fun getNumberOfCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter,
    ): Int

    /**
     * A function for querying companies by search string:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
        "WITH filtered_text_results AS (" +
            // Fuzzy-Search Company Name
            " (SELECT stored_companies.company_id, max(stored_companies.company_name) AS company_name," +
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
     * A function for querying companies by search string:
     * - searchString: If not empty, only companies that contain the search string in their name are returned
     * (Prefix-Matches are ordered before Center-Matches,
     * e.g. when searching for "a" Allianz will come before Deutsche Bank)
     */
    @Query(
        nativeQuery = true,
        value =
        "WITH filtered_text_results AS (" +
            // Fuzzy-Search Company Name
            " (SELECT stored_companies.company_id, max(stored_companies.company_name) AS company_name," +
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
            " MIN(filtered_text_results.company_name) AS companyName," +
            " stored_companies.headquarters AS headquarters, " +
            " stored_companies.country_code AS countryCode, " +
            " stored_companies.sector AS sector, " +
            " lei.identifier_value AS lei " +
            " FROM filtered_text_results " +
            " LEFT JOIN (SELECT company_id, MIN(identifier_value) AS identifier_value FROM company_identifiers" +
            " WHERE identifier_type = 'Lei' GROUP BY company_id) lei " +
            " ON lei.company_id = filtered_text_results.company_id " +
            " LEFT JOIN data_meta_information " +
            " ON filtered_text_results.company_id = data_meta_information.company_id AND currently_active = true" +
            " LEFT JOIN stored_companies" +
            " ON filtered_text_results.company_id = stored_companies.company_id" +
            " GROUP BY filtered_text_results.company_id," +
            " stored_companies.headquarters," +
            " stored_companies.country_code," +
            " stored_companies.sector, " +
            " lei.identifier_value " +
            " ORDER BY " +
            " max(dataset_rank) DESC," +
            " MAX(filtered_text_results.match_quality) DESC, companyId " +
            " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun searchCompaniesByNameOrIdentifierAsBasicCompanyInformation(
        @Param("searchString") searchString: String,
        @Param("resultLimit") resultLimit: Int = 100,
        @Param("resultOffset") resultOffset: Int = 0,
    ): List<BasicCompanyInformation>
}
