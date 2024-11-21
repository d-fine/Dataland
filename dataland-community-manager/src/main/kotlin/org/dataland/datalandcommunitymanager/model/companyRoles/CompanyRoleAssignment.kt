package org.dataland.datalandcommunitymanager.model.companyRoles

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- Generic API model ---
 * DTO to represent the assignment of a company role for one company to one user
 * @param companyRole for which the assignment is valid
 * @param companyId of the company for which the company role is assigned
 * @param userId of the user that the company role has been assigned to
 */
data class CompanyRoleAssignment(
    @field:JsonProperty(required = true)
    val companyRole: CompanyRole,
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val userId: String,
)
