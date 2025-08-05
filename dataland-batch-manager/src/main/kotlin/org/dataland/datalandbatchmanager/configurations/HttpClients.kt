package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandbackendutils.services.KeycloakTokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Provides access to different HttpClients
 */
private const val longTimeout = 10L

@Configuration
class HttpClients {
    /**
     * Returns an OkHttpClient that automatically authenticates all requests and has increased read timeout
     */
    @Bean("PatientAuthenticatedOkHttpClient")
    fun getPatientAuthenticatedOkHttpClient(
        @Autowired keycloakTokenManager: KeycloakTokenManager,
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .readTimeout(longTimeout, TimeUnit.MINUTES)
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
