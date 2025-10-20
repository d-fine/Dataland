package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.IsinLeiDataControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.dataland.dataSourcingService.openApiClient.api.ActuatorApi as DataSourcingActuatorApi
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi as BackendActuatorApi

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Value("\${dataland.data-sourcing-service.base-url}") private val dataSourcingServiceBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getCompanyDataControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyDataControllerApi = CompanyDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an ActuatorApi of the backend
     */
    @Bean
    fun getBackendActuatorApi(): BackendActuatorApi = BackendActuatorApi(backendBaseUrl)

    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the community manager
     */
    @Bean
    fun getRequestControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): RequestControllerApi = RequestControllerApi(dataSourcingServiceBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an ActuatorApi of the data sourcing service
     */
    @Bean
    fun getDataSourcingServiceActuatorApi(): DataSourcingActuatorApi = DataSourcingActuatorApi(dataSourcingServiceBaseUrl)

    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getIsinLeiDataControllerApi(
        @Qualifier("PatientAuthenticatedOkHttpClient") patientAuthenticatedOkHttpClient: OkHttpClient,
    ): IsinLeiDataControllerApi = IsinLeiDataControllerApi(backendBaseUrl, patientAuthenticatedOkHttpClient)
}
