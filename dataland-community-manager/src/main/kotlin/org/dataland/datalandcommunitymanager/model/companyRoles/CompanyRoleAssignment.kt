package org.dataland.datalandcommunitymanager.model.companyRoles

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandcommunitymanager.utils.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- Generic API model ---
 * DTO to represent the assignment of a company role for one company to one user
 * @param companyRole for which the assignment is valid
 * @param companyId of the company for which the company role is assigned
 * @param userId of the user that the company role has been assigned to
 */
data class CompanyRoleAssignment(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_DESCRIPTION,
    )
    val companyRole: CompanyRole,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
)
