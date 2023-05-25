package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewStatusEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the meta information of a document
 */
interface DatasetReviewStatusRepository : JpaRepository<DatasetReviewStatusEntity, String>
