package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId

/**
 * The entity storing the information considering one company role assignment
 * @param companyRole for which the assignment is valid
 * @param companyId of the company for which the company role is assigned
 * @param userId of the user that the company role has been assigned to
 */
@Entity
@Table(name = "company_role_assignments")
@IdClass(CompanyRoleAssignmentId::class)
data class CompanyRoleAssignmentEntity(
    @Id
    @Column(name = "company_role")
    @Enumerated(EnumType.STRING)
    val companyRole: CompanyRole,

    @Id
    @Column(name = "company_id")
    val companyId: String,

    @Id
    @Column(name = "user_id")
    val userId: String,
) {
    /**
     * Converts the entity to an API model object
     * @returns the API model object
     */
    fun toApiModel(): CompanyRoleAssignment {
        return CompanyRoleAssignment(
            companyRole = this.companyRole,
            companyId = this.companyId,
            userId = this.userId,
        )
    }
}
