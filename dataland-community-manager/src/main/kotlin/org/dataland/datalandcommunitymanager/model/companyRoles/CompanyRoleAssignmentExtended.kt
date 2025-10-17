package org.dataland.datalandcommunitymanager.model.companyRoles

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * An extension of CompanyRoleAssignment which additionally includes email, first name and last name.
 */
data class CompanyRoleAssignmentExtended(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_DESCRIPTION,
    )
    val companyRole: CompanyRole,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_USER_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_USER_EMAIL_ADDRESS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    )
    val email: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_FIRST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.FIRST_NAME_EXAMPLE,
    )
    val firstName: String?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_LAST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.LAST_NAME_EXAMPLE,
    )
    val lastName: String?,
)
