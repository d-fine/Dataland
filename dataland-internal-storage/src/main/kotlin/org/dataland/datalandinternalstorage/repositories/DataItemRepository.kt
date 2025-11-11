package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the data items
 */
interface DataItemRepository : JpaRepository<DataItem, String> {
    /**
     * Finds all data items where the data column contains the specified document ID
     * Uses native SQL with LIKE to perform the search at the database level
     * @param documentId the document ID to search for
     * @return list of DataItem entities containing references to the document
     */
    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_items WHERE data LIKE CONCAT('%', :documentId, '%')",
    )
    fun findByDataContainingDocumentId(
        @Param("documentId") documentId: String,
    ): List<DataItem>
}
