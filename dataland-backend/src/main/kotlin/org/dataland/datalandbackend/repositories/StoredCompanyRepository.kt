package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.ReducedCompanyEntity
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
        nativeQuery = true,
        value = "SELECT ids_only.company_id as companyId, " +
            "max(company.company_name) as companyName, " +
            "max(company.headquarters) as headquarters, " +
            "max(company.sector) as sector, " +
            "max(permId.identifier_value) as permId, " +
            "CASE " +
            "WHEN max(company.company_name) ILIKE :#{escape(#searchFilter.searchString)} ESCAPE :#{escapeCharacter()} THEN 1 " +
//            "WHEN MAX(alt_names.company_alternative_names) ILIKE 'e' THEN 2 " +
            "WHEN max(company.company_name) ILIKE :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()} THEN 3 " +
//            "WHEN MAX(alt_names.company_alternative_names) ILIKE 'e%' ESCAPE '' THEN 4 " +
            "ELSE 5 " +
            "END as search_rank " +
            "FROM (SELECT company_id FROM stored_companies " +
            "WHERE (:#{#searchFilter.sectorFilterSize} = 0 OR sector in :#{#searchFilter.sectorFilter}) " +
            "AND (:#{#searchFilter.countryCodeFilterSize} = 0 OR sector in :#{#searchFilter.countryCodeFilter}) " +
            ") ids_only " +
            "JOIN (SELECT distinct company_id from data_meta_information where :#{#searchFilter.dataTypeFilterSize} = 0 OR data_type in :#{#searchFilter.dataTypeFilter}) datainfo " +
            "ON ids_only.company_id = datainfo.company_id " +
            "LEFT JOIN (SELECT * FROM stored_companies WHERE company_name ILIKE '%' || :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()} ) company " +
            "ON ids_only.company_id = company.company_id " +
            "LEFT JOIN (SELECT * FROM company_identifiers where identifier_value ILIKE '%' || :#{escape(#searchFilter.searchString)} || '%' ESCAPE :#{escapeCharacter()} ) identifiers " +
            "ON company.company_id = identifiers.company_id " +
            "LEFT JOIN (SELECT * from company_identifiers where identifier_type = 'PermId') permid " +
            "ON company.company_id = permid.company_id " +
//            "LEFT JOIN stored_company_entity_company_alternative_names alt_names  " +
//            "ON company.company_id = alt_names.stored_company_entity_company_id " +
//            "AND alt_names.company_alternative_names ILIKE '%e%' " +
            "GROUP BY ids_only.company_id " +
            "ORDER BY search_rank asc, max(company.company_name) asc"
    )
    fun searchCompanies(
        @Param("searchFilter") searchFilter: StoredCompanySearchFilter
    ): List<ReducedCompanyEntity>

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
