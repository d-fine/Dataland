package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.DataSourcingEntity
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
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesDocuments " +
            "LEFT JOIN FETCH dataSourcing.associatedRequests " +
            "WHERE dataSourcing.id = :id",
    )
    fun findByIdAndFetchAllStoredFields(id: UUID): DataSourcingEntity?

    /**
     * Find a DataSourcingEntity by the triple companyId, dataType and reportingPeriod and fetch
     * all lazily loaded fields.
     */
    @Query(
        "SELECT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.documentIds " +
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesDocuments " +
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
            "LEFT JOIN FETCH dataSourcing.expectedPublicationDatesDocuments " +
            "WHERE dataSourcing.documentCollector = :companyId ",
    )
    fun findAllByDocumentCollectorAndFetchNonRequestFields(companyId: UUID): List<DataSourcingEntity>

    /**
     * Find all DataSourcingEntity instances by companyId of data extractor.
     */
    fun findAllByDataExtractor(companyId: UUID): List<DataSourcingEntity>
}
