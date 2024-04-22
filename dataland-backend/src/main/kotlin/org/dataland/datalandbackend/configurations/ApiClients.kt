package org.dataland.datalandbackend.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("dataland.community-manager.base-url") private val communitymanagerBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the RequestControllerApi of the community manager
     */
    @Bean
    fun getRequestApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): RequestControllerApi {
        return RequestControllerApi(communitymanagerBaseUrl, authenticatedOkHttpClient)
    }
}
