package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * A JPA repository for accessing the CompanyRoleAssignment Entity
 */
interface CompanyRoleAssignmentRepository : JpaRepository<CompanyRoleAssignmentEntity, CompanyRoleAssignmentId> {

    /** Queries for company role assignments with the provided params
     * @param companyRole to check for
     * @param companyId to check for
     * @param userId to check for
     * @returns a list of the matching company role assignments
     */
    @Query(
        "SELECT roleAssignment FROM CompanyRoleAssignmentEntity roleAssignment " +
            "WHERE (:companyId IS NULL OR roleAssignment.companyId = :companyId) " +
            "AND (:companyRole IS NULL OR roleAssignment.companyRole = :companyRole) " +
            "AND (:userId IS NULL OR roleAssignment.userId = :userId)",
    )
    fun getCompanyRoleAssignmentsByProvidedParameters(
        companyId: String?,
        userId: String?,
        companyRole: CompanyRole?,
    ): List<CompanyRoleAssignmentEntity>
}
