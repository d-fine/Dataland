package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataIdToAssetIdMappingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * A JPA repository for accessing DataIdToAssetIdMappingEntity
 */
interface DataIdToAssetIdMappingRepository : JpaRepository<DataIdToAssetIdMappingEntity, String> {
    /**
     * Retrieves the eurodatId of a document, which is uniquely identified by the combination of dataId and assetId
     */
    @Query("SELECT m FROM DataIdToAssetIdMappingEntity m WHERE m.dataId = :dataId AND m.assetId = :assetId")
    fun findByDataIdAndAssetId(dataId: String, assetId: String): List<DataIdToAssetIdMappingEntity>
}
