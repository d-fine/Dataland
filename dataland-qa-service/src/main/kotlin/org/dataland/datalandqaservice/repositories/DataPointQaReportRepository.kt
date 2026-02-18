package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * A JPA repository for QA reports.
 */
@Repository
interface DataPointQaReportRepository : JpaRepository<DataPointQaReportEntity, String> {
    /**
     * Returns all QA reports for a specific dataPointId. Supports filtering by reporterUserId and active status.
     * @param dataPointIds identifier used to uniquely specify data in the data store
     * @param showInactive flag to include inactive reports in the result
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "SELECT qaReport FROM DataPointQaReportEntity qaReport " +
            "WHERE qaReport.dataPointId IN :dataPointIds " +
            "AND (:showInactive = TRUE OR qaReport.active = TRUE) " +
            "AND (:reporterUserId IS NULL OR qaReport.reporterUserId = :reporterUserId)",
    )
    fun searchQaReportMetaInformation(
        @Param("dataPointIds") dataPointIds: List<String>,
        @Param("showInactive") showInactive: Boolean,
        @Param("reporterUserId") reporterUserId: String?,
    ): List<DataPointQaReportEntity>

    /**
     * Marks all reports for a specific dataPointId and reporterUserId as inactive.
     * @param dataPointId identifier used to uniquely specify data in the data store
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "UPDATE DataPointQaReportEntity qaReport " +
            "SET qaReport.active = FALSE " +
            "WHERE qaReport.dataPointId = :dataPointId " +
            "AND qaReport.reporterUserId = :reporterUserId",
    )
    @Modifying
    fun markAllReportsInactiveByDataPointIdAndReportingUserId(
        dataPointId: String,
        reporterUserId: String,
    )

    /**
     * Returns the number of QA reports where dataPointId is in the given set of ids.
     * @param dataPointIds set of dataPointId values to filter by
     * @return number of matching QA reports
     */
    @Query(
        "SELECT COUNT(qaReport) FROM DataPointQaReportEntity qaReport " +
            "WHERE qaReport.dataPointId IN :dataPointIds " +
            "AND qaReport.active = TRUE",
    )
    fun countByDataPointIdIn(
        @Param("dataPointIds") dataPointIds: Set<String>,
    ): Long

    /**
     * Makes testing easier
     */
    @Query(
        "select qaReport.dataPointType " +
            "from DataPointQaReportEntity qaReport " +
            "where qaReport.qaReportId = :qaReportId",
    )
    fun findDataPointTypeUsingId(qaReportId: String): String
}
