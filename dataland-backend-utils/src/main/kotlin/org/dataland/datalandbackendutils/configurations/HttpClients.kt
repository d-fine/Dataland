package org.dataland.datalandbackendutils.configurations

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.dataland.datalandbackendutils.services.KeycloakTokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Provides access to different HttpClients
 */
@Configuration
class HttpClients {
    private companion object {
        const val LONG_TIMEOUT = 10L // Timeout in minutes

        // The default OkHttp ConnectionPool only keeps 5 idle HTTP/1.1 connections, which is too small once many
        // concurrent requests (e.g. multiple parallel data/document manager batch calls) share this client. Under
        // that load, connections get evicted/reused rapidly, which can otherwise contribute to corrupted HTTP/1.1
        // exchange state (surfacing as `IllegalStateException: state: 0` in OkHttp) on connection reuse.
        const val MAX_IDLE_CONNECTIONS = 20
        const val KEEP_ALIVE_DURATION_MINUTES = 5L
    }

    private val sharedConnectionPool =
        ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION_MINUTES, TimeUnit.MINUTES)

    /**
     * Returns an OkHttpClient that automatically authenticates all requests
     */
    @Bean("AuthenticatedOkHttpClient")
    @ConditionalOnBean(KeycloakTokenManager::class)
    fun getAuthenticatedOkHttpClient(
        @Autowired keycloakTokenManager: KeycloakTokenManager,
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .connectionPool(sharedConnectionPool)
            .addInterceptor {
                val originalRequest = it.request()
                val accessToken = keycloakTokenManager.getAccessToken()
                val modifiedRequest =
                    originalRequest
                        .newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                it.proceed(modifiedRequest)
            }.build()

    /**
     * The getter for a standard OkHttpClient
     */
    @Bean("UnauthenticatedOkHttpClient")
    fun getOkHttpClient(): OkHttpClient = OkHttpClient()

    /**
     * Returns an OkHttpClient that automatically authenticates all requests and has increased read timeout
     */
    @Bean("PatientAuthenticatedOkHttpClient")
    @ConditionalOnBean(KeycloakTokenManager::class)
    fun getPatientAuthenticatedOkHttpClient(
        @Autowired keycloakTokenManager: KeycloakTokenManager,
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .connectionPool(sharedConnectionPool)
            .readTimeout(LONG_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor {
                val originalRequest = it.request()
                val accessToken = keycloakTokenManager.getAccessToken()
                val modifiedRequest =
                    originalRequest
                        .newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                it.proceed(modifiedRequest)
            }.build()
}
