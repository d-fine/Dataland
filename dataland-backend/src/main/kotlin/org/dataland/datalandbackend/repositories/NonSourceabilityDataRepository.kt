package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository for canonical non-sourceability lifecycle records.
 */
interface NonSourceabilityDataRepository : JpaRepository<NonSourceabilityInformationEntity, UUID> {
    /**
     * Finds the first (most recent) non-sourceability record for a company-dataType-reportingPeriod combination.
     *
     * @param companyId the company identifier
     * @param dataType the type of data
     * @param reportingPeriod the reporting period
     * @return the most recent non-sourceability information entity, or null if not found
     */
    fun findFirstByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): NonSourceabilityInformationEntity?

    /**
     * Finds all non-sourceability records for a company-dataType-reportingPeriod combination.
     *
     * @param companyId the company identifier
     * @param dataType the type of data
     * @param reportingPeriod the reporting period
     * @return a list of non-sourceability information entities
     */
    fun findAllByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): List<NonSourceabilityInformationEntity>

    /**
     * Checks if a non-sourceability record exists with the specified QA statuses.
     *
     * @param companyId the company identifier
     * @param dataType the type of data
     * @param reportingPeriod the reporting period
     * @param qaStatus the collection of QA statuses to check
     * @return true if a matching record exists, false otherwise
     */
    fun existsByCompanyIdAndDataTypeAndReportingPeriodAndQaStatusIn(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        qaStatus: Collection<QaStatus>,
    ): Boolean

    /**
     * Searches non-sourceability information based on filter criteria.
     *
     * @param searchFilter the search filter containing filter criteria
     * @return a list of matching non-sourceability information entities
     */
    @Query(
        "SELECT info FROM NonSourceabilityInformationEntity info " +
            "WHERE " +
            "(:#{#searchFilter.shouldFilterByCompanyId} = false OR " +
            "info.companyId = :#{#searchFilter.preparedCompanyId}) AND " +
            "(:#{#searchFilter.shouldFilterByDataType} = false OR " +
            "info.dataType = :#{#searchFilter.preparedDataType}) AND " +
            "(:#{#searchFilter.shouldFilterByReportingPeriod} = false OR " +
            "info.reportingPeriod = :#{#searchFilter.preparedReportingPeriod}) AND " +
            "(:#{#searchFilter.shouldFilterByQaStatus} = false OR " +
            "info.qaStatus = :#{#searchFilter.preparedQaStatus}) AND " +
            "(:#{#searchFilter.shouldFilterByCurrentlyActive} = false OR " +
            "info.currentlyActive = :#{#searchFilter.preparedCurrentlyActive}) " +
            "ORDER BY info.uploadTime DESC",
    )
    fun searchNonSourceabilityInformation(
        @Param("searchFilter") searchFilter: NonSourceableDataSearchFilter,
    ): List<NonSourceabilityInformationEntity>
}
