package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataIdToAssetIdMappingEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing DataIdToAssetIdMappingEntities
 */
interface DataIdToAssetIdMappingRepository : JpaRepository<DataIdToAssetIdMappingEntity, String> {
    /**
     * Retrieves the eurodatId of a document, which is uniquely identified by the combination of dataId and assetId
     */
    fun findByDataIdAndAssetId(dataId: String, assetId: String): List<DataIdToAssetIdMappingEntity>
}
