package org.dataland.datalandcommunitymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * Contains the basic information about a Dataland user needed to
 * process the addition of the user to a company in a membership role.
 */
data class BasicUserInformation(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_USER_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_USER_EMAIL_ADDRESS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    )
    val email: String? = null,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_FIRST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.FIRST_NAME_EXAMPLE,
    )
    val firstName: String? = null,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_LAST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.LAST_NAME_EXAMPLE,
    )
    val lastName: String? = null,
)
