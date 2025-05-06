package org.dataland.datalandbackendutils.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A data class mirroring Keycloak's RoleRepresentation class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakRoleRepresentation(
    @JsonProperty("id")
    val roleId: String?,
    @JsonProperty("name")
    val roleName: String,
)
