package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the data items
 */
interface DataItemRepository : JpaRepository<DataItem, String> {
    /**
     * Finds all data items where the data column contains the specified string
     * @param dataSearchString the string to search for within the data column
     * @return list of DataItem entities containing the specified string
     */
    fun findByDataContaining(dataSearchString: String): List<DataItem>
}
