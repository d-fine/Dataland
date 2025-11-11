package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the data point items
 */
interface DataPointItemRepository : JpaRepository<DataPointItem, String> {
    /**
     * Finds all data point items where the data_point column contains the specified document ID
     * Uses native SQL with LIKE to perform the search at the database level
     * @param documentId the document ID to search for
     * @return list of DataPointItem entities containing references to the document
     */
    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_point_items WHERE data LIKE CONCAT('%', :documentId, '%')",
    )
    fun findByDataPointContainingDocumentId(
        @Param("documentId") documentId: String,
    ): List<DataPointItem>
}
