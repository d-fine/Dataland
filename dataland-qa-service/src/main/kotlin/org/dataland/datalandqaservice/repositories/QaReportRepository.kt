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
        "SELECT qaReport FROM QaReportEntity qaReport " +
            "WHERE qaReport.dataId = :dataId " +
            "AND (:showInactive = TRUE OR qaReport.active = TRUE) " +
            "AND (:reporterUserId IS NULL OR qaReport.reporterUserId = :reporterUserId)",
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

    /**
     * Deletes all QA reports for a specific dataId.
     */
    fun deleteAllByDataId(dataId: String)

    /**
     * Returns all QA reports for a set of specific dataIds. Supports filtering by a date range
     * and active status. The date range is characterized by startDate and endDate and can be open
     * to both sides.
     * @param dataIds identifiers used to uniquely specify data in the data store
     * @param onlyActive whether to only show active reports
     * @param startDate show only QA reports uploaded by the given user
     * @param endDate show only QA reports uploaded by the given user
     */
    @Query(
        "SELECT qaReport FROM QaReportEntity qaReport " +
            "WHERE qaReport.dataId IN :dataIds " +
            "AND (:onlyActive = FALSE OR qaReport.active = TRUE) " +
            "AND (qaReport.uploadTime IS NULL " +
            "OR (qaReport.uploadTime >= COALESCE(:startDate, qaReport.uploadTime) " +
            "AND qaReport.uploadTime <= COALESCE(:endDate, qaReport.uploadTime)))",
    )
    fun searchQaReportMetaInformation(
        dataIds: List<String>,
        onlyActive: Boolean,
        startDate: Long?,
        endDate: Long?,
    ): List<QaReportEntity>
}
