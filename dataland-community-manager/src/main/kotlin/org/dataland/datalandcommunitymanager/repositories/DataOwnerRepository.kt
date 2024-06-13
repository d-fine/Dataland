package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.CompanyDataOwnersEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the CompanyDataOwners Entity
 */
interface DataOwnerRepository : JpaRepository<CompanyDataOwnersEntity, String>
