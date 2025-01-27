package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the data point items
 */
interface DataPointItemRepository : JpaRepository<DataPointItem, String>
