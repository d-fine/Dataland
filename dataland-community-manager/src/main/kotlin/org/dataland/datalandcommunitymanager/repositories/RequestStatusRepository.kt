package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for storing and retrieving request status entities
 */

interface RequestStatusRepository : JpaRepository<RequestStatusEntity, String>
