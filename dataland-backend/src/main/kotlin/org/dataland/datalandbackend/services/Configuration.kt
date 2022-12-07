package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 */
//@Component
/*class NewStorageControllerApi {
   // @Autowired(required = false)
    val newStorageControllerApi = StorageControllerApi()
}*/
@Configuration
class ConfigurationStorageClient {
    var baseUrl: String? = null

    /**
     * The bean to configure the EDCConnectorInterface
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi{
        return StorageControllerApi(
            basePath = "http://localhost:8082/internal-storage"
        )
    }
}