package org.dataland.datalandbackendutils.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * Sub-set of the properties that are returned when you request user info from keycloak via user endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserInfo(
    @JsonProperty("email")
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_USER_EMAIL_ADDRESS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    )
    val email: String?,
    @JsonProperty("id", required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_USER_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @JsonProperty("firstName")
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_FIRST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.FIRST_NAME_EXAMPLE,
    )
    val firstName: String?,
    @JsonProperty("lastName")
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.GENERAL_LAST_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.LAST_NAME_EXAMPLE,
    )
    val lastName: String?,
)
