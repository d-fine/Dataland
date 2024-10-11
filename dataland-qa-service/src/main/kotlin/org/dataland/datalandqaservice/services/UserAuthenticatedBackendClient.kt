package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Provides access to pre-configured Backend Api Client that is authenticated as a specific user.
 */
@Service
class UserAuthenticatedBackendClient(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    private fun getUserAuthenticatedHttpClient(authentication: DatalandAuthentication): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .addInterceptor {
                val originalRequest = it.request()
                val modifiedRequest =
                    originalRequest
                        .newBuilder()
                        .header("Authorization", "Bearer ${authentication.credentials}")
                        .build()
                it.proceed(modifiedRequest)
            }.build()

    /**
     * Builds a MetaDataControllerApi that is authenticated as the given Dataland User.
     */
    fun getMetaDataControllerApiForUserAuthentication(authentication: DatalandAuthentication): MetaDataControllerApi {
        val userAuthenticatedApiClient = getUserAuthenticatedHttpClient(authentication)
        return MetaDataControllerApi(backendBaseUrl, userAuthenticatedApiClient)
    }
}
