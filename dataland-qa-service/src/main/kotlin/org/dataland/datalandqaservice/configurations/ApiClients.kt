package org.dataland.datalandqaservice.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Value("\${dataland.specification-service.base-url}") private val specificationServiceBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getCompanyDataControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyDataControllerApi = CompanyDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the MetaDataControllerApi of the backend
     */
    @Bean
    fun getMetaDataControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): MetaDataControllerApi = MetaDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the DataPointControllerApi of the backend
     */
    @Bean
    fun getDataPointControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): DataPointControllerApi = DataPointControllerApi(backendBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an auto-authenticated version of the SpecificationControllerApi of the specification service
     */
    @Bean
    fun getSpecificationControllerApi(): SpecificationControllerApi = SpecificationControllerApi(specificationServiceBaseUrl)
}
