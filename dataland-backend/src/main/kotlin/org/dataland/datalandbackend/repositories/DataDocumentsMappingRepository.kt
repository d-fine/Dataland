package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataIdToAssetIdMappingEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataIdToAssetIdMappingRepository : JpaRepository<DataIdToAssetIdMappingEntity, String>
