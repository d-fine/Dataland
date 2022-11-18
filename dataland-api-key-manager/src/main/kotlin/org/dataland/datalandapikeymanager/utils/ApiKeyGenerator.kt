package org.dataland.datalandapikeymanager.utils

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.model.ApiKeyData
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import java.util.HexFormat


class ApiKeyGenerator {

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

    fun getNewApiKey(daysValid: Long?): ApiKeyData {
        val username = SecurityContextHolder.getContext().authentication.principal.toString()
        val role = "NOT IMPLEMENTED"
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid)
        val newApiKey = generateApiKey()
        return ApiKeyData(username, role, expiryDate, newApiKey)
    }
}
