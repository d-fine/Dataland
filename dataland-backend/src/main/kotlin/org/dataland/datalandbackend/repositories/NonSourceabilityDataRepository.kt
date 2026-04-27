package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * JPA repository for [NonSourceabilityInformationEntity].
 *
 * Provides filtered search and existence checks used by the canonical non-sourceability
 * workflow. [SourceabilityDataRepository] continues to exist as backup-only storage.
 */
interface NonSourceabilityDataRepository : JpaRepository<NonSourceabilityInformationEntity, UUID> {
    /**
     * Returns entries matching optional filters on companyId, dataType, reportingPeriod, and qaStatus.
     * Null parameters are treated as "no filter".
     */
    @Query(
        """
        SELECT e FROM NonSourceabilityInformationEntity e
        WHERE (:companyId IS NULL OR e.companyId = :companyId)
          AND (:dataType IS NULL OR e.dataType = :dataType)
          AND (:reportingPeriod IS NULL OR e.reportingPeriod = :reportingPeriod)
          AND (:qaStatus IS NULL OR e.qaStatus = :qaStatus)
        ORDER BY e.uploadTime DESC
        """,
    )
    fun findByFilters(
        @Param("companyId") companyId: String?,
        @Param("dataType") dataType: DataType?,
        @Param("reportingPeriod") reportingPeriod: String?,
        @Param("qaStatus") qaStatus: QaStatus?,
    ): List<NonSourceabilityInformationEntity>

    /**
     * Returns true if there is already a row with qaStatus Pending or Accepted for the given tuple.
     * Used for duplicate-request rejection.
     */
    @Query(
        """
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM NonSourceabilityInformationEntity e
        WHERE e.companyId = :companyId
          AND e.dataType = :dataType
          AND e.reportingPeriod = :reportingPeriod
          AND e.qaStatus IN :statuses
        """,
    )
    fun existsWithGivenStatuses(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
        @Param("statuses") statuses: List<QaStatus>,
    ): Boolean

    /**
     * Returns the most recent currently-active entry for the tuple, or null if none exists.
     * Used by the HEAD endpoint existence check.
     */
    @Query(
        """
        SELECT e FROM NonSourceabilityInformationEntity e
        WHERE e.companyId = :companyId
          AND e.dataType = :dataType
          AND e.reportingPeriod = :reportingPeriod
          AND e.currentlyActive = true
        ORDER BY e.uploadTime DESC
        """,
    )
    fun findActiveForTuple(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: DataType,
        @Param("reportingPeriod") reportingPeriod: String,
    ): List<NonSourceabilityInformationEntity>
}
