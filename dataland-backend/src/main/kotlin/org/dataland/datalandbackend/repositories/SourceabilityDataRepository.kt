package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing NonSourceableEntities
 */
interface SourceabilityDataRepository : JpaRepository<SourceabilityEntity, String> {
    /**
     * Retrieves the most recent non-sourceable dataset entry matching the specified reporting period, company ID,
     * and data type. The results are filtered and sorted by the given criteria, and only the latest entry is returned.
     * @param searchFilter An object containing the filtering criteria: reporting period, company ID, and data type.
     * @return The most recent NonSourceableEntity that matches the filter criteria, or `null` if no match is found.
     */

    @Query(
        "SELECT nonSourceableData FROM SourceabilityEntity nonSourceableData " +
            "WHERE :#{#searchFilter.reportingPeriod != null} = true " +
            "AND nonSourceableData.reportingPeriod = :#{#searchFilter.reportingPeriod} " +
            "AND :#{#searchFilter.companyId != null} = true " +
            "AND nonSourceableData.companyId = :#{#searchFilter.companyId} " +
            "AND :#{searchFilter.dataType != null} = true " +
            "AND nonSourceableData.dataType = :#{#searchFilter.dataType} " +
            "ORDER BY nonSourceableData.creationTime DESC " +
            "LIMIT 1",
    )
    fun getLatestSourceabilityInfoForDataset(
        @Param("searchFilter") searchFilter: NonSourceableDataSearchFilter,
    ): SourceabilityEntity?

    /**
     * Searches for non-sourceable data entries based on a filter defined in NonSourceableDataSearchFilter.
     * The filtering parameters are companyId, dataType, reportingPeriod, and isNonSourceable.
     *
     * If a filter for companyId, dataType, or reportingPeriod is set (non-null),
     * only entries matching the criteria will be returned. If the filter is not set, this criterion is ignored.
     * The query is designed to return results matching all specified criteria. If no entries match the criteria, an empty list is returned.
     *
     * @param searchFilter The set of filtering criteria encapsulated in a NonSourceableDataSearchFilter object.
     * @return A list of NonSourceableEntity instances that match the specified filter criteria.
     * Returns an empty list if no matches are found.
     */
    @Query(
        "SELECT nonSourceableData FROM SourceabilityEntity nonSourceableData " +
            "WHERE " +
            "(:#{#searchFilter.companyId == null} = true " +
            "OR nonSourceableData.companyId = :#{#searchFilter.companyId}) AND " +
            "(:#{#searchFilter.dataType == null} = true " +
            "OR nonSourceableData.dataType = :#{#searchFilter.dataType}) AND " +
            "(:#{#searchFilter.reportingPeriod == null} = true " +
            "OR nonSourceableData.reportingPeriod = :#{#searchFilter.reportingPeriod}) AND " +
            "(:#{#searchFilter.isNonSourceable == null} = true " +
            "OR nonSourceableData.isNonSourceable = :#{#searchFilter.isNonSourceable}) " +
            "ORDER BY nonSourceableData.creationTime DESC",
    )
    fun searchNonSourceableData(
        @Param("searchFilter") searchFilter: NonSourceableDataSearchFilter,
    ): List<SourceabilityEntity>
}
