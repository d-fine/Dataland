package org.dataland.datalandapikeymanager.utils

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.StoredHashedApiKey
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import java.util.HexFormat

class ApiKeyGenerator {

    // TODO temporary
    private val mapOfUsernameAndStoredHashedApiKey = mutableMapOf<String, StoredHashedApiKey>()
    private val logger = LoggerFactory.getLogger(javaClass)

    private val keyByteLength = 40
    private val saltByteLength = 16
    private val hashByteLength = 32

    private fun generateRandomByteArray(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun generateSalt(): ByteArray {
        return generateRandomByteArray(saltByteLength)
    }

    private fun generateApiKey(): String {
        return HexFormat.of().formatHex(generateRandomByteArray(keyByteLength))
    }

    private fun encodeToStorageFormat(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    private fun decodeFromStorageFormat(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    private fun hashApiKey(apiKey: String, salt: ByteArray): String {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(3)
            .withMemoryPowOfTwo(16)
            .withParallelism(1)
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(builder.build())
        val hash = ByteArray(hashByteLength)
        generator.generateBytes(apiKey.toByteArray(), hash, 0, hash.size)
        return encodeToStorageFormat(hash)
    }

    fun getNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val username = SecurityContextHolder.getContext().authentication.principal.toString()
        val role = SecurityContextHolder.getContext().authentication.authorities.map { it.authority!! }.toList()
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid.toLong())
        val newApiKey = generateApiKey()
        val newSalt = generateSalt()
        val newSaltEncoded = encodeToStorageFormat(newSalt)
        val newHashedApiKeyEncoded = hashApiKey(newApiKey, newSalt)

        // TODO Storage process => needs to be in postgres. map is just temporary
        val apiKeyMetaInfo = ApiKeyMetaInfo(username, role, expiryDate)
        val storedHashedApiKey = StoredHashedApiKey(newHashedApiKeyEncoded, apiKeyMetaInfo, newSaltEncoded)
        mapOfUsernameAndStoredHashedApiKey[username] = storedHashedApiKey
        logger.info("Generated Api Key with hashed value $newHashedApiKeyEncoded and meta info ${apiKeyMetaInfo}.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    fun validateApiKey(username: String, apiKey: String): ApiKeyMetaInfo {

        // TODO Validation process => needs to be in postgres. map is just temporary
        val storedHashedApiKey = mapOfUsernameAndStoredHashedApiKey[username]!!
        val salt = decodeFromStorageFormat(storedHashedApiKey.salt)
        val hashedApiKey = hashApiKey(apiKey, salt)

        logger.info("Validated Api Key with salt $salt and calculated hash value $hashedApiKey.")
        return storedHashedApiKey.apiKeyMetaInfo
    }
}
