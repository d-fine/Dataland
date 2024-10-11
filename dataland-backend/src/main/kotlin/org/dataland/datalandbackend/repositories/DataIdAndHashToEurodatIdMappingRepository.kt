package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataIdAndHashToEurodatIdMappingEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing DataIdToHashMappingEntities
 */
interface DataIdAndHashToEurodatIdMappingRepository : JpaRepository<DataIdAndHashToEurodatIdMappingEntity, String> {
    /**
     * Retrieves the eurodatId of a document, which is uniquely identified by the combination of dataId and hash
     */
    fun findByDataIdAndHash(
        dataId: String,
        hash: String,
    ): DataIdAndHashToEurodatIdMappingEntity?
}
