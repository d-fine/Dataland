package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class containing the relevant aspects of the keycloak access token response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakAccessTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,
)
