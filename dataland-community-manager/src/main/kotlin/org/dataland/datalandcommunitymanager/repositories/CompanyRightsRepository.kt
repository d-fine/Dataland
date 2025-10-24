package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.CompanyRightEntity
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for accessing the company_rights table.
 */
interface CompanyRightsRepository : JpaRepository<CompanyRightEntity, CompanyRightAssignment> {
    /**
     * Find all company rights for a given company ID.
     */
    fun findAllByCompanyId(companyId: UUID): List<CompanyRightEntity>
}
