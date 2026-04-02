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
     * Projection for grouped count results returned by [countByDataPointIdGroups].
     */
    interface GroupedQaReportCount {
        /**
         *  The index of the group as determined by the order of the input JSON array in [countByDataPointIdGroups].
         */
        fun getGroupIndex(): Int

        /**
         *  The count of active QA reports for the dataPointIds in the corresponding group.
         */
        fun getReportCount(): Long
    }

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
     * Counts active QA reports for multiple dataPointId groups in a single database query.
     * The parameter must be a JSON array of arrays, for example: [["dp1","dp2"],["dp3"]].
     */
    @Query(
        value =
            "WITH input_groups AS (" +
                "    SELECT CAST(group_with_index.ordinality - 1 AS INTEGER) AS group_index, group_with_index.group_json " +
                "    FROM jsonb_array_elements(CAST(:groupedDataPointIdsJson AS jsonb)) " +
                "WITH ORDINALITY AS group_with_index(group_json, ordinality)" +
                ")," +
                "group_data_points AS (" +
                "    SELECT ig.group_index, extracted_data_points.data_point_id " +
                "    FROM input_groups ig " +
                "    LEFT JOIN LATERAL (" +
                "        SELECT DISTINCT jsonb_array_elements_text(ig.group_json) AS data_point_id" +
                "    ) extracted_data_points ON TRUE" +
                ")," +
                "grouped_counts AS (" +
                "    SELECT ig.group_index, COUNT(qa_report.qa_report_id) AS report_count " +
                "    FROM input_groups ig " +
                "    LEFT JOIN group_data_points group_data_point " +
                "        ON group_data_point.group_index = ig.group_index " +
                "    LEFT JOIN data_point_qa_reports qa_report " +
                "        ON qa_report.data_point_id = group_data_point.data_point_id " +
                "        AND qa_report.active = TRUE " +
                "    GROUP BY ig.group_index" +
                ") " +
                "SELECT grouped_count.group_index AS groupIndex, grouped_count.report_count AS reportCount " +
                "FROM grouped_counts grouped_count " +
                "ORDER BY grouped_count.group_index",
        nativeQuery = true,
    )
    fun countByDataPointIdGroups(
        @Param("groupedDataPointIdsJson") groupedDataPointIdsJson: String,
    ): List<GroupedQaReportCount>

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
