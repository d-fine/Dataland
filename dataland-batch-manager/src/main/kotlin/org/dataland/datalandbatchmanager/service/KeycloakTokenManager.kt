package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dataland.datalandbatchmanager.model.KeycloakAccessTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class KeycloakTokenManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Value("\${dataland.dataland-batch-manager.client-id}") private val clientId: String,
    @Value("\${dataland.dataland-batch-manager.client-secret}") private val clientSecret: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var currentAccessToken: String? = null
    private var currentAccessTokenExpireTime: Instant? = null

    fun getAccessToken(): String {
        if (currentAccessToken == null || Instant.now().until(currentAccessTokenExpireTime, ChronoUnit.SECONDS) < 30) {
            updateAccessToken()
        }
        return currentAccessToken!!
    }

    private fun updateAccessToken() {
        logger.info("Updating Keycloak Access Token.")
        val authorizationHeader = "Basic ${Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())}"

        val client = OkHttpClient()
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "grant_type=client_credentials".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://keycloak:8080/keycloak/realms/datalandsecurity/protocol/openid-connect/token")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", authorizationHeader)
            .build()
        val response = client.newCall(request).execute()

        val parsedResponseBody = objectMapper.readValue<KeycloakAccessTokenResponse>(response.body!!.string())

        currentAccessToken = parsedResponseBody.accessToken
        currentAccessTokenExpireTime = Instant.now() + Duration.ofSeconds(parsedResponseBody.expiresIn.toLong())
        logger.info("Acquired new access token!")
    }
}
