package org.dataland.datalandapikeymanager.entities

import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Table

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
