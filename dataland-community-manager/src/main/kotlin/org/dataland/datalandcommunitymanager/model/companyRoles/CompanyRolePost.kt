package org.dataland.datalandcommunitymanager.model.companyRoles

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.util.UUID

/**
 * Request body of the endpoint for posting a company role assignment.
 */
data class CompanyRolePost(
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
    val companyId: UUID,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_USER_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: UUID?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_EMAIL_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_EMAIL_EXAMPLE,
    )
    val email: String?,
)
