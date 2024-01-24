package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.MessageRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * JPA repository for accessing the MessageRequestEntity
 */
interface MessageRequestRepository : JpaRepository<MessageRequestEntity, String>