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
     * Returns all QA reports for a specific dataId. Supports filtering by reporterUserId and active status.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param showInactive flag to include inactive reports in the result
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "SELECT qaReport FROM DataPointQaReportEntity qaReport " +
            "WHERE qaReport.dataPointId = :dataId " +
            "AND (:showInactive = TRUE OR qaReport.active = TRUE) " +
            "AND (:reporterUserId IS NULL OR qaReport.reporterUserId = :reporterUserId)",
    )
    fun searchQaReportMetaInformation(
        @Param("dataId") dataId: String,
        @Param("showInactive") showInactive: Boolean,
        @Param("reporterUserId") reporterUserId: String?,
    ): List<DataPointQaReportEntity>

    /**
     * Marks all reports for a specific dataId and reporterUserId as inactive.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "UPDATE DataPointQaReportEntity qaReport " +
            "SET qaReport.active = FALSE " +
            "WHERE qaReport.dataPointId = :dataId " +
            "AND qaReport.reporterUserId = :reporterUserId",
    )
    @Modifying
    fun markAllReportsInactiveByDataIdAndReportingUserId(
        dataId: String,
        reporterUserId: String,
    )
}
