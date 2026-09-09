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
 * Provides filtered search and existence checks used for the non-sourceability workflow.
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
    ): NonSourceabilityInformationEntity?

    /**
     * Returns all currently-active non-sourceability entries matching the given filters.
     * An empty list for any parameter is treated as a wildcard (no restriction on that dimension),
     * indicated by the corresponding "isXEmpty" flag being true (in which case the list itself is ignored).
     * Note on the query structure: for each filter, we pass a separate boolean flag
     * (e.g. "isDataTypesEmpty") instead of writing "(:dataTypes IS NULL OR e.dataType IN :dataTypes)".
     *
     * Reason: the "dataType" column is not a plain String column - it's mapped via
     * DataTypeConverter, which converts between the DataType Kotlin class and its
     * String representation in the database. When the same query parameter
     * (":dataTypes") is used both for an "IS NULL" check and for an "IN" list check,
     * Hibernate gets confused about which conversion to apply to the list elements.
     * Instead of using DataTypeConverter as expected, it tries to serialize the
     * DataType objects directly via plain Java serialization - which fails at runtime
     * with a NotSerializableException, because DataType does not implement Serializable.
     *
     * Used for bulk triple search (e.g. POST /non-sourceable/search).
     */
    @Query(
        """
        SELECT e FROM NonSourceabilityInformationEntity e
        WHERE (:isCompanyIdsEmpty = true OR e.companyId IN :companyIds)
          AND (:isDataTypesEmpty = true OR e.dataType IN :dataTypes)
          AND (:isReportingPeriodsEmpty = true OR e.reportingPeriod IN :reportingPeriods)
          AND e.currentlyActive = true
        """,
    )
    fun findActiveTriples(
        @Param("companyIds") companyIds: List<String>,
        @Param("isCompanyIdsEmpty") isCompanyIdsEmpty: Boolean,
        @Param("dataTypes") dataTypes: List<DataType>,
        @Param("isDataTypesEmpty") isDataTypesEmpty: Boolean,
        @Param("reportingPeriods") reportingPeriods: List<String>,
        @Param("isReportingPeriodsEmpty") isReportingPeriodsEmpty: Boolean,
    ): List<NonSourceabilityInformationEntity>
}
