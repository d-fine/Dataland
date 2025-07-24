package org.dataland.datalandbackendutils.configurations

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
    @Suppress("MagicNumber")
    fun getPatientAuthenticatedOkHttpClient(
        @Autowired keycloakTokenManager: KeycloakTokenManager,
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .readTimeout(10, TimeUnit.MINUTES)
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
