package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the meta info associated to an API key
 * @param keycloakUserId of the owning Keycloak user
 * @param keycloakRoles defines the roles of the owning Keycloak user
 * @param expiryDate specifies until which date the API key can be used
 * @param active specifies if the associated api key
 */
data class ApiKeyMetaInfo(
    @field:JsonProperty(required = false)
    val keycloakUserId: String? = null,

    @field:JsonProperty(required = false)
    val keycloakRoles: List<String>? = null,

    @field:JsonProperty(required = false)
    val expiryDate: LocalDate? = null,

    // TODO This should not be stored in DB, because it is calculated on runtime via expiryDate
    @field:JsonProperty(required = true)
    val active: Boolean,
)
