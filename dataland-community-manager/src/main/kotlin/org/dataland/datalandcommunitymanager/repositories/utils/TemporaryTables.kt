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
            "(:#{#searchFilter.shouldFilterByDataType} = false " +
            "OR d.data_type IN :#{#searchFilter.usedDataTypeFilter}) AND " +
            "(:#{#searchFilter.shouldFilterByUserId} = false " +
            "OR d.user_Id = :#{#searchFilter.usedUserIdFilter}) AND " +
            "(:#{#searchFilter.shouldFilterByEmailAddress} = false " +
            "OR d.user_Id IN :#{#prefetchedUserIdsByEmail}) AND " +
            "(:#{#searchFilter.shouldFilterByRequestStatus} = false " +
            "OR status_table.request_status IN :#{#searchFilter.usedRequestStatusFilter} ) AND " +
            "(:#{#searchFilter.shouldFilterByAccessStatus} = false " +
            "OR status_table.access_status IN :#{#searchFilter.usedAccessStatusFilter}  ) AND " +
            "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
            "OR d.reporting_period = :#{#searchFilter.usedReportingPeriodFilter}) AND " +
            "(:#{#searchFilter.shouldFilterByDatalandCompanyId} = false " +
            "OR d.dataland_company_id = :#{#searchFilter.usedDatalandCompanyIdFilter}) " +
            "ORDER BY d.data_request_id ASC " +
            "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}) "
    }
}
