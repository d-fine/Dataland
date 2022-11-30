package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandapikeymanager.entities.ApiKeyEntity

/**
 * --- API model ---
 * Class for defining the meta info associated to an API key
 * @param keycloakUserId is the user Id of the owning Keycloak user
 * @param keycloakRoles defines the roles of the owning Keycloak user
 * @param expiryDate specifies until which date the API key can be used
 * @param active specifies if the associated API key is active
 * @param validationMessage returns a message associated to a validation of the associated API key
 */
data class ApiKeyMetaInfo(
    @field:JsonProperty(required = false)
    val keycloakUserId: String? = null,

    @field:JsonProperty(required = false)
    val keycloakRoles: List<String>? = null,

    @field:JsonProperty(required = false)
    val expiryDate: Long? = null,

    @field:JsonProperty(required = false)
    val active: Boolean? = null,

    @field:JsonProperty(required = false)
    val validationMessage: String? = null
) {
    constructor(apiKeyEntity: ApiKeyEntity, active: Boolean?, validationMessage: String?) :
        this(
            apiKeyEntity.keycloakUserId,
            apiKeyEntity.keycloakRoles,
            apiKeyEntity.expiryDate,
            active,
            validationMessage
        )
}
