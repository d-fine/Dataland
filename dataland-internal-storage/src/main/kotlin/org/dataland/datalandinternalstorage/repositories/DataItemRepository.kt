package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the data items
 */
interface DataItemRepository : JpaRepository<DataItem, String> {
}