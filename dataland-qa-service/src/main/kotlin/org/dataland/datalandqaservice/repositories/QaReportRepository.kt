package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for QA reports.
 */
interface QaReportRepository : JpaRepository<QaReportEntity, String> {
    /**
     * Returns all QA reports for a specific dataId. Supports filtering by reporterUserId and active status.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param showInactive flag to include inactive reports in the result
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "SELECT qaReportMetaInformation FROM QaReportEntity qaReportMetaInformation " +
            "WHERE qaReportMetaInformation.dataId = :dataId " +
            "AND (:showInactive = TRUE OR qaReportMetaInformation.active = TRUE) " +
            "AND (:reporterUserId IS NULL OR qaReportMetaInformation.reporterUserId = :reporterUserId)",
    )
    fun searchQaReportMetaInformation(
        @Param("dataId") dataId: String,
        @Param("showInactive") showInactive: Boolean,
        @Param("reporterUserId") reporterUserId: String?,
    ): List<QaReportEntity>

    /**
     * Marks all reports for a specific dataId and reporterUserId as inactive.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param reporterUserId show only QA reports uploaded by the given user
     */
    @Query(
        "UPDATE QaReportEntity qaReport " +
            "SET qaReport.active = FALSE " +
            "WHERE qaReport.dataId = :dataId " +
            "AND qaReport.reporterUserId = :reporterUserId",
    )
    @Modifying
    fun markAllReportsInactiveByDataIdAndReportingUserId(dataId: String, reporterUserId: String)
}
