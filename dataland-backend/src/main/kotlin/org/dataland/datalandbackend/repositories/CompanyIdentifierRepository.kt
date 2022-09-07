package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierId
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA interface for accessing the CompanyIdentifier Entity
 */
interface CompanyIdentifierRepository : JpaRepository<CompanyIdentifierEntity, CompanyIdentifierId>
