package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the CompanyRoleAssignment Entity
 */
interface CompanyRoleAssignmentRepository : JpaRepository<CompanyRoleAssignmentEntity, CompanyRoleAssignmentId> {
    /** Queries for company role assignments with the provided params
     * @param companyId to check for
     * @param companyRole to check for
     * @returns a list of the matching company role assignments
     */
    fun findByCompanyIdAndCompanyRole(companyId: String, companyRole: CompanyRole): List<CompanyRoleAssignmentEntity>

    /** Queries for company role assignments with the provided params
     * @param userId to check for
     * @param companyRole to check for
     * @returns a list of the matching company role assignments
     */
    fun findByUserIdAndCompanyRole(userId: String, companyRole: CompanyRole): List<CompanyRoleAssignmentEntity>
}
