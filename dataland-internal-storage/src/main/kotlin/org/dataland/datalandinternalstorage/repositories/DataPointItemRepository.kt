package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the data point items
 */
interface DataPointItemRepository : JpaRepository<DataPointItem, String> {
    /**
     * Finds all data point items where the dataPoint column contains the specified string
     * @param dataSearchString the string to search for within the dataPoint column
     * @return list of DataPointItem entities containing the specified string
     */
    fun findByDataPointContaining(dataSearchString: String): List<DataPointItem>
}
