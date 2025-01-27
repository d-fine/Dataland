package org.dataland.datalandbackend.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
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
    @Value("\${dataland.specification-service.base-url}") private val specificationServiceBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the CompanyRolesControllerApi of the community manager
     */
    @Bean
    fun getCompanyRolesApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyRolesControllerApi = CompanyRolesControllerApi(communitymanagerBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the RequestControllerApi of the community manager
     */
    @Bean
    fun getRequestsApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): RequestControllerApi = RequestControllerApi(communitymanagerBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the DocumentControllerApi of the document manager
     */
    @Bean
    fun getDocumentControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): DocumentControllerApi = DocumentControllerApi(documentManagerBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the SpecificationServiceControllerApi of the specification service
     */
    @Bean
    fun getSpecificationControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): SpecificationControllerApi = SpecificationControllerApi(specificationServiceBaseUrl, authenticatedOkHttpClient)
}
