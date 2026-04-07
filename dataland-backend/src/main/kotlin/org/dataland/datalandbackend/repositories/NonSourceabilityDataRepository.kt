package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * A JPA repository for accessing NonSourceabilityInformationEntity instances.
 */
interface NonSourceabilityDataRepository : JpaRepository<NonSourceabilityInformationEntity, UUID> {
    /**
     * Retrieves the active non-sourceability request for a given company, data type, and reporting period.
     * An active request has either Pending or Accepted status.
     *
     * @param companyId The company ID to search for
     * @param dataType The data type to search for
     * @param reportingPeriod The reporting period to search for
     * @return The NonSourceabilityInformationEntity if found, null otherwise
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE n.companyId = :companyId " +
            "AND n.dataType = :dataType " +
            "AND n.reportingPeriod = :reportingPeriod " +
            "AND n.qaStatus IN (org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus.Pending, " +
            "org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus.Accepted) " +
            "ORDER BY n.uploadTime DESC " +
            "LIMIT 1",
    )
    fun findActiveRequest(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
    ): NonSourceabilityInformationEntity?

    /**
     * Retrieves all non-sourceability requests with the specified QA status, ordered by upload time (newest first).
     *
     * @param qaStatus The QA status to filter by
     * @return A list of NonSourceabilityInformationEntity instances matching the status
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE n.qaStatus = :qaStatus " +
            "ORDER BY n.uploadTime DESC",
    )
    fun findByQaStatus(
        @Param("qaStatus") qaStatus: QaNonSourceabilityStatus,
    ): List<NonSourceabilityInformationEntity>

    /**
     * Retrieves pending non-sourceability requests for QA review, ordered by upload time (oldest first).
     * This is used for queue-based QA task assignment.
     *
     * @return A list of pending NonSourceabilityInformationEntity instances
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE n.qaStatus = org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus.Pending " +
            "ORDER BY n.uploadTime ASC",
    )
    fun findPendingRequests(): List<NonSourceabilityInformationEntity>

    /**
     * Checks if any accepted or pending non-sourceability claim exists for the given data dimensions.
     *
     * @param companyId The company ID to check
     * @param dataType The data type to check
     * @param reportingPeriod The reporting period to check
     * @return true if an active non-sourceability claim exists, false otherwise
     */
    @Query(
        "SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
            "FROM NonSourceabilityInformationEntity n " +
            "WHERE n.companyId = :companyId " +
            "AND n.dataType = :dataType " +
            "AND n.reportingPeriod = :reportingPeriod " +
            "AND n.qaStatus IN (org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus.Pending, " +
            "org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus.Accepted)",
    )
    fun existsActiveNonSourceabilityRequest(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
    ): Boolean

    /**
     * Retrieves all non-sourceability requests for a specific company, optionally filtered by data type and QA status.
     *
     * @param companyId The company ID to search for
     * @param dataType Optional data type to filter by (null to include all data types)
     * @param qaStatus Optional QA status to filter by (null to include all statuses)
     * @return A list of matching NonSourceabilityInformationEntity instances
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE n.companyId = :companyId " +
            "AND (:dataType IS NULL OR n.dataType = :dataType) " +
            "AND (:qaStatus IS NULL OR n.qaStatus = :qaStatus) " +
            "ORDER BY n.uploadTime DESC",
    )
    fun findByCompanyFiltered(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType?,
        @Param("qaStatus") qaStatus: QaNonSourceabilityStatus?,
    ): List<NonSourceabilityInformationEntity>

    /**
     * Retrieves non-sourceability requests by optional data dimensions.
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE (:companyId IS NULL OR n.companyId = :companyId) " +
            "AND (:dataType IS NULL OR n.dataType = :dataType) " +
            "AND (:reportingPeriod IS NULL OR n.reportingPeriod = :reportingPeriod) " +
            "ORDER BY n.uploadTime DESC",
    )
    fun findByDimensions(
        @Param("companyId") companyId: String?,
        @Param("dataType") dataType: DataType?,
        @Param("reportingPeriod") reportingPeriod: String?,
    ): List<NonSourceabilityInformationEntity>

    /**
     * Retrieves the latest non-sourceability request for exact data dimensions.
     */
    @Query(
        "SELECT n FROM NonSourceabilityInformationEntity n " +
            "WHERE n.companyId = :companyId " +
            "AND n.dataType = :dataType " +
            "AND n.reportingPeriod = :reportingPeriod " +
            "ORDER BY n.uploadTime DESC " +
            "LIMIT 1",
    )
    fun findLatestByDimensions(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
    ): NonSourceabilityInformationEntity?
}
