package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for the singleton pre-approval configuration row.
 */
@Repository
interface QaConfigRepository : JpaRepository<QaConfigEntity, UUID>
