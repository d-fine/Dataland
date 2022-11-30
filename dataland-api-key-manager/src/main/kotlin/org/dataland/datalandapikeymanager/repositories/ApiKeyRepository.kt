package org.dataland.datalandapikeymanager.repositories

import org.dataland.datalandapikeymanager.entities.ApiKeyEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the hashed and Base64 encoded API keys of Dataland (Keycloak) users together with
 * meta info for those API keys
 */
interface ApiKeyRepository : JpaRepository<ApiKeyEntity, String>
