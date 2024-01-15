package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the CompanyDataOwners Entity
 */
interface DataOwnerRepository : JpaRepository<CompanyDataOwnersEntity, String>
