package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA Repository to match an existing dataset to the datapoints it is composed of.
 * Exists for legacy purposes.
 */
interface DatasetDatapointRepository : JpaRepository<DatasetDatapointEntity, String>
