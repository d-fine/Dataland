package org.dataland.datalandbackendutils.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Sub-set of the properties that are returned when you request user info from keycloak via user endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserInfo(
    @JsonProperty("email")
    val email: String?,

    @JsonProperty("id")
    val userId: String,

    @JsonProperty("firstName")
    val firstName: String?,

    @JsonProperty("lastName")
    val lastName: String?,
)
