package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for accessing dataset review information.
 */
@Repository
interface DatasetReviewRepository : JpaRepository<DatasetReviewEntity, UUID>
