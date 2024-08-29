package org.dataland.datalandcommunitymanager.repositories.utils

/**
 * A temporary table class used in different repository interfaces which allows
 * convenient usage of temporary tables in the query
 */
class TemporaryTables private constructor() {

    companion object {
        // Defines a SELECT on the REQUEST_STATUS_HISTORY table to get the most recent status change
        const val MOST_RECENT_STATUS_CHANGE = "WITH most_recent AS (SELECT data_request_id, MAX(creation_timestamp) " +
            "AS creation_timestamp FROM request_status_history " +
            "GROUP BY data_request_id), " +
            "status_table AS (SELECT most_recent.data_request_id AS request_id, request_status, access_status " +
            "FROM request_status_history " +
            "JOIN most_recent ON most_recent.data_request_id = request_status_history.data_request_id " +
            "AND most_recent.creation_timestamp = request_status_history.creation_timestamp) "

        // Select data_request_id, based on specified filters
        const val TABLE_FILTERED = MOST_RECENT_STATUS_CHANGE +
            ", " +
            "filtered_table AS (SELECT d.data_request_id " +
            "FROM data_requests d " +
            "JOIN status_table ON status_table.request_id = d.data_request_id " +
            "WHERE " +
            "(:#{#searchFilter.dataTypeFilterLength} = 0 " +
            "OR d.data_type = :#{#searchFilter.dataTypeFilter}) AND " +
            "(:#{#searchFilter.userIdFilterLength} = 0 " +
            "OR d.user_Id = :#{#searchFilter.userIdFilter}) AND " +
            "(:#{searchFilter.shouldApplyEmailFilter} = false " +
            "OR d.user_Id IN :#{#searchFilter.appliedUserIdsFromEmailFilter}) AND " +
            "(:#{#searchFilter.requestStatusLength} = 0 " +
            "OR status_table.request_status = :#{#searchFilter.requestStatus} ) AND " +
            "(:#{#searchFilter.accessStatusLength} = 0 " +
            "OR status_table.access_status = :#{#searchFilter.accessStatus}  ) AND " +
            "(:#{#searchFilter.reportingPeriodFilterLength} = 0 " +
            "OR d.reporting_period = :#{#searchFilter.reportingPeriodFilter}) AND " +
            "(:#{#searchFilter.datalandCompanyIdFilterLength} = 0 " +
            "OR d.dataland_company_id = :#{#searchFilter.datalandCompanyIdFilter}) " +
            "ORDER BY d.data_request_id ASC " +
            "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) "
    }
}
