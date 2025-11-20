package org.dataland.documentmanager.api

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
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
    @Value("\${dataland.internalstorage.base-url}") private val internalStorageBaseUrl: String,
    @Value("\${dataland.qaservice.base-url}") private val qaServiceBaseUrl: String,
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

    /**
     * Creates an auto-authenticated version of the StorageControllerApi of the internal storage
     */
    @Bean
    fun getStorageControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): StorageControllerApi = StorageControllerApi(internalStorageBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the QaControllerApi of the qa service
     */
    @Bean
    fun getQaControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): QaControllerApi = QaControllerApi(qaServiceBaseUrl, authenticatedOkHttpClient)
}
