package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing NonSourceableEntities
 */
interface NonSourceableDataRepository : JpaRepository<NonSourceableEntity, String> {
    /**
     * Retrieves non sourceable datasets for the given triplet of reporting Period, company and dataType
     */

    @Query(
        "SELECT nonSourceableData FROM NonSourceableEntity nonSourceableData " +
            "WHERE nonSourceableData.reportingPeriod = :#{#searchFilter.preparedReportingPeriod} " +
            "AND nonSourceableData.companyId = :#{#searchFilter.preparedCompanyId} " +
            "AND nonSourceableData.dataType = :#{#searchFilter.preparedDataType} " +
            "ORDER BY nonSourceableData.creationTime DESC " +
            "LIMIT 1",
    )
    fun getLatestNonSourceableData(
        @Param("searchFilter") searchFilter: NonSourceableDataSearchFilter,
    ): NonSourceableEntity?

    /**
     * Searches for non-sourceable data entries based on a filter defined in NonSourceableDataSearchFilter.
     * The filtering parameters are companyId, dataType, reportingPeriod, and nonSourceable.
     *
     * If a filter for companyId, dataType, or reportingPeriod is set (non-null or non-empty),
     * only entries matching the criteria will be returned. If the filter is not set, this criterion is ignored.
     * The nonSourceable filter allows filtering for entries where the `nonSourceable` field is either `true` or `false`.
     * If the filter is not set (null), no filtering is applied on the `nonSourceable` field.
     * The query is designed to return results matching all specified criteria. If no entries match the criteria, an empty list is returned.
     *
     * @param searchFilter The set of filtering criteria encapsulated in a NonSourceableDataSearchFilter object.
     * @return A list of NonSourceableEntity instances that match the specified filter criteria.
     * Returns an empty list if no matches are found.
     */
    @Query(
        "SELECT nonSourceableData FROM NonSourceableEntity nonSourceableData " +
            "WHERE " +
            "(:#{#searchFilter.shouldFilterByCompanyId} = false " +
            "OR nonSourceableData.companyId = :#{#searchFilter.preparedCompanyId}) AND " +
            "(:#{#searchFilter.shouldFilterByDataType} = false " +
            "OR nonSourceableData.dataType = :#{#searchFilter.preparedDataType}) AND " +
            "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
            "OR nonSourceableData.reportingPeriod = :#{#searchFilter.preparedReportingPeriod}) AND " +
            "(:#{#searchFilter.shouldFilterByNonSourceable} = false " +
            "OR nonSourceableData.isNonSourceable = :#{#searchFilter.preparedNonSourceable}) " +
            "ORDER BY nonSourceableData.creationTime DESC",
    )
    fun searchNonSourceableData(
        @Param("searchFilter") searchFilter: NonSourceableDataSearchFilter,
    ): List<NonSourceableEntity>
}
