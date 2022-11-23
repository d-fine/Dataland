package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the meta info associated to an API key
 * @param keycloakUserId of the owning Keycloak user
 * @param keycloakRoles defines the roles of the owning Keycloak user
 * @param expiryDate specifies until which date the API key can be used
 */
data class ApiKeyMetaInfo(
    @field:JsonProperty(required = true)
    val keycloakUserId: String,

    @field:JsonProperty(required = true)
    val keycloakRoles: List<String>,

    @field:JsonProperty(required = true)
    val expiryDate: LocalDate?,
)
