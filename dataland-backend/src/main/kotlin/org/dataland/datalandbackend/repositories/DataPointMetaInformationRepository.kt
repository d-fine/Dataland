package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataPointMetaInformationRepository : JpaRepository<DataPointMetaInformationEntity, UUID>
