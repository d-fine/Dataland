package org.dataland.documentmanager.api

import okhttp3.OkHttpClient
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
) {
    /**
     * Creates an auto-authenticated version of the CompanyRolesControllerApi of the community manager
     */
    @Bean
    fun getCompanyRolesControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyRolesControllerApi {
        return CompanyRolesControllerApi(communityManagerBaseUrl, authenticatedOkHttpClient)
    }
}
