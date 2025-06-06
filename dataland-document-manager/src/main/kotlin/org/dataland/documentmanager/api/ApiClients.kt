package org.dataland.documentmanager.api

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.communitymanager.base-url}") private val communityManagerBaseUrl: String,
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the CompanyRolesControllerApi of the community manager
     */
    @Bean
    fun getCompanyRolesControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyRolesControllerApi = CompanyRolesControllerApi(communityManagerBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the CompanyControllerApi of the backend
     */
    @Bean
    fun getCompanyControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyDataControllerApi = CompanyDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)
}
