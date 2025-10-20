package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

/**
 * A JPA repository for accessing the CompanyRoleAssignment Entity
 */
interface CompanyRoleAssignmentRepository : JpaRepository<CompanyRoleAssignmentEntity, CompanyRoleAssignmentId> {
    /** Queries for company role assignments with the provided params
     * @param companyRole to check for
     * @param companyId to check for
     * @param userId to check for
     * @return a list of the matching company role assignments
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

    /**
     * Finds a company role assignment for a specific user, company and one of the provided roles.
     * Since a user can only have one role per company, there can only be at most one matching result.
     * @param companyId to check for
     * @param userId to check for
     * @param companyRoles to check for
     * @return the matching company role assignment or null if none found
     */
    fun findByCompanyIdAndUserIdAndCompanyRoleIsIn(
        companyId: String,
        userId: String,
        companyRoles: List<CompanyRole>,
    ): CompanyRoleAssignmentEntity?

    /** Deletes all company role assignments for a specific user and company
     * @param companyId to delete the assignments for
     * @param userId to delete the assignments for
     */
    @Modifying
    @Transactional
    @Query(
        "DELETE FROM CompanyRoleAssignmentEntity roleAssignment " +
            "WHERE roleAssignment.companyId = :companyId " +
            "AND roleAssignment.userId = :userId",
    )
    fun deleteAllRolesByCompanyIdAndUserId(
        companyId: String,
        userId: String,
    )
}
