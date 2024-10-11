package org.dataland.datalandbackendutils.services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

/**
 * Class to manage the token retrieval from keycloak via service account
 */
@Service
@ConditionalOnProperty(name = ["dataland.keycloak.client-id"])
class KeycloakTokenManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Qualifier("UnauthenticatedOkHttpClient") private val httpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
    @Value("\${dataland.keycloak.client-id}") private val clientId: String,
    @Value("\${dataland.keycloak.client-secret}") private val clientSecret: String,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KeycloakAccessTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
    )

    companion object {
        private const val LIFETIME_THRESHOLD_IN_SECONDS = 30
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private var currentAccessToken: String? = null
    private var currentAccessTokenExpireTime: Instant? = null

    /**
     * Triggers the update of the access token if required
     * @return the value of the currently used access token
     */
    @Synchronized
    fun getAccessToken(): String {
        if (currentAccessToken == null ||
            Instant.now().until(currentAccessTokenExpireTime, ChronoUnit.SECONDS) < LIFETIME_THRESHOLD_IN_SECONDS
        ) {
            updateAccessToken()
        }
        return currentAccessToken!!
    }

    private fun updateAccessToken() {
        logger.info("Updating Keycloak Access Token.")
        val authorizationHeader = Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "grant_type=client_credentials".toRequestBody(mediaType)
        val request =
            Request
                .Builder()
                .url("$keycloakBaseUrl/realms/datalandsecurity/protocol/openid-connect/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic $authorizationHeader")
                .build()
        val response = httpClient.newCall(request).execute()
        val parsedResponseBody =
            objectMapper.readValue(
                response.body!!.string(),
                KeycloakAccessTokenResponse::class.java,
            )
        currentAccessToken = parsedResponseBody.accessToken
        currentAccessTokenExpireTime = Instant.now() + Duration.ofSeconds(parsedResponseBody.expiresIn.toLong())
        logger.info("Acquired new access token!")
    }
}
