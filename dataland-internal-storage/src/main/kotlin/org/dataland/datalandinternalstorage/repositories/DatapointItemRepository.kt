package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DatapointItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the data items
 */
interface DatapointItemRepository : JpaRepository<DatapointItem, String>
