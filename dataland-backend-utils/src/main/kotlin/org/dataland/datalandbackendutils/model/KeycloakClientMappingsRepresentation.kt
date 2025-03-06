package org.dataland.datalandbackendutils.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A data class mirroring Keycloak's ClientMappingsRepresentation class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakClientMappingsRepresentation(
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("client")
    val client: String?,
    @JsonProperty("mappings")
    val mappings: List<KeycloakRoleRepresentation>?,
)
