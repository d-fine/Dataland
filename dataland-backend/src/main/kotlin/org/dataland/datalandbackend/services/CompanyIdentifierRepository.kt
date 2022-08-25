package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyIdentifierId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyIdentifierRepository : JpaRepository<CompanyIdentifier, CompanyIdentifierId> {
}