package org.dataland.datalandapikeymanager.utils

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.StoredHashedApiKey
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import java.util.HexFormat

class ApiKeyGenerator {

    // TODO temporary
    private val mapOfKeycloakUserIdsAndStoredHashedApiKeys = mutableMapOf<String, StoredHashedApiKey>()
    // TODO temporary

    private val logger = LoggerFactory.getLogger(javaClass)

    private val keyByteLength = 40
    private val saltByteLength = 16
    private val hashByteLength = 32

    private val utf8Charset = Charsets.UTF_8

    private fun hashString(inputString: String, salt: ByteArray): ByteArray {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(3)
            .withMemoryPowOfTwo(16)
            .withParallelism(1)
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(builder.build())
        val hash = ByteArray(hashByteLength)
        generator.generateBytes(inputString.toByteArray(), hash, 0, hash.size)
        return hash
    }

    private fun generateRandomByteArray(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun encodeToBase64(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    private fun decodeFromBase64(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    private fun generateSalt(): ByteArray {
        return generateRandomByteArray(saltByteLength)
    }

    private fun generateApiKeySecretAndEncodeToHex(): String {
        return HexFormat.of().formatHex(generateRandomByteArray(keyByteLength))
    }
    
    private fun getKeycloakAuthenticationToken(): KeycloakAuthenticationToken {
        return SecurityContextHolder.getContext().authentication as KeycloakAuthenticationToken
    }
    
    private fun getKeycloakUserId(keycloakAuthenticationToken: KeycloakAuthenticationToken): String {
        var userIdByToken = ""
        val principal = keycloakAuthenticationToken.principal
        if (principal is KeycloakPrincipal<*>) {
            userIdByToken = principal.keycloakSecurityContext.token.subject
        }
        return userIdByToken
    }

    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val keycloakAuthenticationToken = getKeycloakAuthenticationToken()
        val userId = getKeycloakUserId(keycloakAuthenticationToken)
        val userIdBase64Encoded = encodeToBase64(userId.toByteArray(utf8Charset))
        val roles = keycloakAuthenticationToken.authorities.map { it.authority!! }.toList()
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid.toLong())
        val apiKeyMetaInfo = ApiKeyMetaInfo(userId, roles, expiryDate)

        val newApiKey = userIdBase64Encoded+"_"+generateApiKeySecretAndEncodeToHex()
        val newSalt = generateSalt()
        val newSaltBase64Encoded = encodeToBase64(newSalt)

        val newHashedApiKeyBase64Encoded = encodeToBase64(hashString(newApiKey, newSalt))
        val storedHashedApiKey = StoredHashedApiKey(newHashedApiKeyBase64Encoded, apiKeyMetaInfo, newSaltBase64Encoded)

        // TODO Storage process => needs to be in postgres. map is just temporary
        mapOfKeycloakUserIdsAndStoredHashedApiKeys[userId] = storedHashedApiKey
        // TODO

        logger.info("Generated Api Key with hashed value $newHashedApiKeyBase64Encoded and meta info ${apiKeyMetaInfo}.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    fun validateApiKey(apiKey: String): ApiKeyMetaInfo {
        val userIdBase64Encoded = apiKey.substringBefore("_")
        val userId = decodeFromBase64(userIdBase64Encoded).toString(utf8Charset)

        // TODO Validation process => needs to be in postgres. map is just temporary
        val storedHashedApiKey = mapOfKeycloakUserIdsAndStoredHashedApiKeys[userId]!!
        // TODO

        val salt = decodeFromBase64(storedHashedApiKey.saltBase64Encoded)
        val hashedApiKeyBase64Encoded = encodeToBase64(hashString(apiKey, salt))

        if (hashedApiKeyBase64Encoded == storedHashedApiKey.hashedApiKeyBase64Encoded) {
            logger.info("Validated Api Key with salt $salt and calculated hash value $hashedApiKeyBase64Encoded.")
        }
        return storedHashedApiKey.apiKeyMetaInfo
    }
}
