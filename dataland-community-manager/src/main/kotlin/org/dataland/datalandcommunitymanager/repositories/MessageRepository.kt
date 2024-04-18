package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for storing and retrieving data requests
 */

interface MessageRepository : JpaRepository<MessageEntity, String>
