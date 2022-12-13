package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.RequestMetaDataEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the metadata items
 */
interface RequestMetaDataRepository : JpaRepository<RequestMetaDataEntity, String>
