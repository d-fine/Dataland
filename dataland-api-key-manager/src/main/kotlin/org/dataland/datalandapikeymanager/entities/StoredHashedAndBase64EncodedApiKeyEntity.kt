package org.dataland.datalandapikeymanager.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandapikeymanager.interfaces.ApiModelConversion
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.StoredHashedAndBase64EncodedApiKey
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Table

/**
 * The entity storing hashed and encoded API keys together with meta info
 */
@Entity
@Table(name = "stored_hashed_and_encoded_api_keys")
data class StoredHashedAndBase64EncodedApiKeyEntity(
    @Column(name = "api_key_hashed_and_encoded")
    val apiKeyHashedAndBase64Encoded: String,

    @Id
    @Column(name = "keycloak_user_id_encoded")
    var keycloakUserIdBase64Encoded: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "keycloak_roles")
    var keycloakRoles: List<String>,

    @Column(name = "expiry_date")
    var expiryDate: LocalDateTime?,

    @Column(name = "salt_encoded")
    var saltBase64Encoded: String,
) : ApiModelConversion<StoredHashedAndBase64EncodedApiKey> {
    @JsonValue
    override fun toApiModel(): StoredHashedAndBase64EncodedApiKey {
        return StoredHashedAndBase64EncodedApiKey(
            apiKeyHashedAndBase64Encoded = apiKeyHashedAndBase64Encoded,
            apiKeyMetaInfo = ApiKeyMetaInfo(
                keycloakUserIdBase64Encoded = keycloakUserIdBase64Encoded,
                keycloakRoles = keycloakRoles,
                expiryDate = expiryDate
            ),
            saltBase64Encoded = saltBase64Encoded
        )
    }
}
