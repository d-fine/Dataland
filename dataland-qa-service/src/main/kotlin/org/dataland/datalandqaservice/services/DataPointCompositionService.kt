package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * A service class for managing the composition of datasets
 */
@Service
class DataPointCompositionService
    @Autowired
    constructor(
        private val metaDataControllerApi: MetaDataControllerApi,
    ) {
        /**
         * Gets the composition of a dataset as a map of DataPointId to DataPointDataId
         * Returns null if the dataset is not a composition of data points
         */
        fun getCompositionOfDataset(dataId: String): Map<String, String>? =
            try {
                metaDataControllerApi.getContainedDataPoints(dataId)
            } catch (ex: ClientException) {
                if (ex.statusCode == HttpStatus.NOT_FOUND.value()) {
                    null
                } else {
                    throw ex
                }
            }
    }
