package org.dataland.datalandbackendutils.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A data class mirroring Keycloak's MappingsRepresentation class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakMappingsRepresentation(
    @JsonProperty("realmMappings")
    val realmMappings: List<KeycloakRoleRepresentation> = emptyList(),
    @JsonProperty("clientMappings")
    val clientMappings: Map<String, KeycloakClientMappingsRepresentation> = emptyMap(),
)
