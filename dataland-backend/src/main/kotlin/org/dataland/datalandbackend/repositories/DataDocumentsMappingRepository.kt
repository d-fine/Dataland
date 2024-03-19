package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataDocumentMappingEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataDocumentsMappingRepository : JpaRepository<DataDocumentMappingEntity, String>
