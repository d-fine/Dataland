package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the meta info associated to an API key
 * @param keycloakUserIdBase64Encoded is the user Id of the owning Keycloak user encoded with Base64
 * @param keycloakRoles defines the roles of the owning Keycloak user
 * @param expiryDate specifies until which date the API key can be used
 * @param active specifies if the associated API key is active
 * @param validationMessage returns a message associated to a validation of the associated API key
 */
data class ApiKeyMetaInfo(
    @field:JsonProperty(required = false)
    val keycloakUserIdBase64Encoded: String? = null,

    @field:JsonProperty(required = false)
    val keycloakRoles: List<String>? = null,

    @field:JsonProperty(required = false)
    val expiryDate: LocalDate? = null,

    @field:JsonProperty(required = false)
    val active: Boolean? = null,

    @field:JsonProperty(required = false)
    val validationMessage: String? = null
)
