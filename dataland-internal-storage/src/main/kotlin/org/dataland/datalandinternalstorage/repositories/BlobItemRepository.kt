package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.BlobItem
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the stored blobs
 */
interface BlobItemRepository : JpaRepository<BlobItem, String>
