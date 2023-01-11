package org.dataland.datalandapikeymanager.entities

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo

/**
 * The entity storing hashed and encoded API keys together with meta info
 */
@Entity
@Table(name = "api_keys")
data class ApiKeyEntity(

    val encodedSecret: String,

    @Id
    var keycloakUserId: String,

    @ElementCollection(fetch = FetchType.EAGER)
    var keycloakRoles: List<String>,

    var expiryDate: Long?,
) {
    constructor(encodedSecret: String, apiKeyMetaInfo: ApiKeyMetaInfo) :
        this(encodedSecret, apiKeyMetaInfo.keycloakUserId!!, apiKeyMetaInfo.keycloakRoles!!, apiKeyMetaInfo.expiryDate)
}
