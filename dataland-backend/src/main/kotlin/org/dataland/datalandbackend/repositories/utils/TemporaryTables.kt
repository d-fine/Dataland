package org.dataland.datalandbackend.repositories.utils

/**
 * A temporary table class used in different repository interfaces which allows
 * convenient usage of temporary tables in the query
 */
class TemporaryTables private constructor() {
    companion object {
        // Select company_id, LEI identifiers as leis
        const val TABLE_LEIS =
            " ( " +
                " SELECT identifier_value, company_id " +
                " FROM company_identifiers " +
                " WHERE identifier_type = 'Lei' " +
                " ) AS leis "

        // Column for the TABLE_FILTERED_TEXT_RESULTS table
        private const val DATASET_RANK =
            " MAX( CASE " +
                "   WHEN data_id IS NOT NULL THEN 2 " +
                "   ELSE 1 " +
                "   END) AS dataset_rank "

        // Select company_id, company_name, match_quality, dataset_rank based on searchString as filtered_text_results
        // Requires the parameter searchFilter : StoredCompanySearchFilter
        const val TABLE_FILTERED_TEXT_RESULTS =
            " ( " +
                " (SELECT stored_companies.company_id, MAX(stored_companies.company_name) AS company_name, " +
                " MAX( CASE " +
                "   WHEN company_name = :#{#searchFilter.searchString} THEN 10 " +
                "   WHEN company_name ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 5 " +
                "   ELSE 1 " +
                "   END) AS match_quality, " +
                DATASET_RANK +
                " FROM stored_companies " +
                " LEFT JOIN data_meta_information " +
                "   ON stored_companies.company_id = data_meta_information.company_id AND currently_active = true " +
                " WHERE company_name ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} " +
                " GROUP BY stored_companies.company_id) " +

                " UNION " +
                // Fuzzy-Search Company Alternative Name
                " (SELECT stored_company_entity_company_id AS company_id," +
                " MAX(stored_companies.company_name) AS company_name, " +
                " MAX( CASE " +
                "   WHEN company_alternative_names = :#{#searchFilter.searchString} THEN 9 " +
                "   WHEN company_alternative_names ILIKE :#{escape(#searchFilter.searchString)}% " +
                "           ESCAPE :#{escapeCharacter()} THEN 4 " +
                "   ELSE 1 " +
                "   END) AS match_quality, " +
                DATASET_RANK +
                " FROM stored_company_entity_company_alternative_names " +
                " JOIN stored_companies " +
                "   ON stored_companies.company_id = " +
                "       stored_company_entity_company_alternative_names.stored_company_entity_company_id " +
                " LEFT JOIN data_meta_information " +
                "   ON stored_company_entity_company_id = data_meta_information.company_id AND currently_active = true " +
                " WHERE company_alternative_names " +
                "       ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} " +
                " GROUP BY stored_company_entity_company_id " +
                " ) " +

                " UNION " +
                // Fuzzy-Search Company Identifier
                " (SELECT company_identifiers.company_id, MAX(stored_companies.company_name) AS company_name, " +
                " MAX( CASE " +
                "   WHEN identifier_value = :#{#searchFilter.searchString} THEN 10 " +
                "   WHEN identifier_value " +
                "           ILIKE :#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} THEN 3 " +
                "   ELSE 0" +
                "   END) AS match_quality, " +
                DATASET_RANK +
                " FROM company_identifiers " +
                " JOIN stored_companies ON stored_companies.company_id = company_identifiers.company_id " +
                " LEFT JOIN data_meta_information " +
                "   ON company_identifiers.company_id = data_meta_information.company_id AND currently_active = true " +
                " WHERE identifier_value ILIKE %:#{escape(#searchFilter.searchString)}% ESCAPE :#{escapeCharacter()} " +
                " GROUP BY company_identifiers.company_id) " +
                " ) AS filtered_text_results "

        // Select company_id if company satisfies data_type, sector and country filter
        // Requires the parameter searchFilter : StoredCompanySearchFilter
        const val TABLE_FILTERED_DROPDOWN_RESULTS =
            " (" +
                " SELECT company_id FROM stored_companies " +
                " WHERE (:#{#searchFilter.dataTypeFilterSize} = 0 OR company_id IN " +
                "   (SELECT DISTINCT company_id " +
                "       FROM data_meta_information " +
                "       WHERE currently_active='true'" +
                "       AND :#{#searchFilter.dataTypeFilterSize} > 0" +
                "       AND data_type IN :#{#searchFilter.dataTypeFilter})) " +
                " AND (:#{#searchFilter.sectorFilterSize} = 0 OR sector IN :#{#searchFilter.sectorFilter}) " +
                " AND (:#{#searchFilter.countryCodeFilterSize} = 0 " +
                "       OR country_code IN :#{#searchFilter.countryCodeFilter}) " +
                " ) AS filtered_dropdown_results "
    }
}
