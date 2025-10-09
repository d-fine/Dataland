package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */
interface DataSourcingRepository : JpaRepository<DataSourcingEntity, UUID> {
    /**
     * Find a DataSourcingEntity by its id and fetch all lazily loaded fields.
     * Return null if no entity with the given id exists.
     */
    @Query(
        "SELECT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.documentIds " +
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesOfDocuments " +
            "LEFT JOIN FETCH dataSourcing.associatedRequests " +
            "WHERE dataSourcing.dataSourcingId = :id",
    )
    fun findByIdAndFetchAllStoredFields(id: UUID): DataSourcingEntity?

    /**
     * Find a DataSourcingEntity by the triple companyId, dataType and reportingPeriod and fetch
     * all lazily loaded fields.
     */
    @Query(
        "SELECT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.documentIds " +
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesOfDocuments " +
            "LEFT JOIN FETCH dataSourcing.associatedRequests " +
            "WHERE dataSourcing.companyId = :companyId " +
            "AND dataSourcing.dataType = :dataType " +
            "AND dataSourcing.reportingPeriod = :reportingPeriod",
    )
    fun findByDataDimensionAndFetchAllStoredFields(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): DataSourcingEntity?

    /**
     * Find all DataSourcingEntity instances by companyId of document collector.
     */
    @Query(
        "SELECT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.documentIds " +
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesOfDocuments " +
            "WHERE dataSourcing.documentCollector = :companyId ",
    )
    fun findAllByDocumentCollectorAndFetchNonRequestFields(companyId: UUID): List<DataSourcingEntity>

    /**
     * Find all DataSourcingEntity instances by companyId of data extractor.
     */
    @Query(
        "SELECT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.documentIds " +
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesOfDocuments " +
            "WHERE dataSourcing.dataExtractor = :companyId ",
    )
    fun findAllByDataExtractor(companyId: UUID): List<DataSourcingEntity>

    /**
     * Search data sourcing entities by the optional filters
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @param pageable for pagination
     * @return List of matching DataSourcingEntity ids
     */
    @Query(
        "SELECT dataSourcingEntity.dataSourcingId FROM DataSourcingEntity dataSourcingEntity " +
            "WHERE " +
            "(:companyId IS NULL OR dataSourcingEntity.companyId = :companyId) AND " +
            "(:dataType IS NULL OR dataSourcingEntity.dataType = :dataType) AND " +
            "(:reportingPeriod IS NULL OR dataSourcingEntity.reportingPeriod = :reportingPeriod) AND " +
            "(:state IS NULL OR dataSourcingEntity.state = :state)",
    )
    fun searchDataSourcingEntities(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: DataSourcingState?,
        pageable: Pageable,
    ): Page<UUID>

/**
     * Find all DataSourcingEntity instances by a list of ids and fetch all lazily loaded fields.
     */
    @Query(
        "SELECT dataSourcingEntity FROM DataSourcingEntity dataSourcingEntity " +
            "LEFT JOIN FETCH dataSourcingEntity.documentIds " +
            "LEFT JOIN FETCH dataSourcingEntity.expectedPublicationDatesOfDocuments " +
            "LEFT JOIN FETCH dataSourcingEntity.associatedRequests " +
            "WHERE " +
            "dataSourcingEntity.dataSourcingId IN :dataSourcingIds",
    )
    fun findByIdsAndFetchAllReferences(dataSourcingIds: List<UUID>?): List<DataSourcingEntity>
}
