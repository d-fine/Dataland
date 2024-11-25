package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
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
            "WHERE nonSourceableData.reportingPeriod = :#{#reportingPeriod} " +
            "AND nonSourceableData.companyId = :#{#companyId} " +
            "AND nonSourceableData.dataType = :#{#dataType} " +
            "AND nonSourceableData.nonSourceable = true " +
            "ORDER BY creation_time DESC" +
            "LIMIT 1",
        nativeQuery = true,
    )
    fun getNonSourceableDataByTriple(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
    )

    /**
     * Retrieves a list of non-sourceable data sets concerning the query parameters.
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriodAndNonSourceable(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): List<NonSourceableEntity>?
}
