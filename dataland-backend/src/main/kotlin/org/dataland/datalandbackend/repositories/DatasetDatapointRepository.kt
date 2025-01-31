package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * A JPA Repository to match an existing dataset to the data points it is composed of.
 */
@Repository
interface DatasetDatapointRepository : JpaRepository<DatasetDatapointEntity, String>
