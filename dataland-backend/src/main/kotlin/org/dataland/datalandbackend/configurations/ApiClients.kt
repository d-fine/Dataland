package org.dataland.datalandbackend.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.community-manager.base-url}") private val communitymanagerBaseUrl: String,
    @Value("\${dataland.documentmanager.base-url}") private val documentManagerBaseUrl: String,
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

    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getDocumentControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): DocumentControllerApi {
        return DocumentControllerApi(documentManagerBaseUrl, authenticatedOkHttpClient)
    }
}
