package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.StorableDataSet

/**
 * Service to store and retrieve datasets from a backing storage engine
 */
interface DatasetStorageService {
    /**
     * Stores a dataset in the backing storage engine
     * @param uploadedDataSet the data set to process
     * @param bypassQa whether to bypass the QA process
     * @param correlationId the correlation id for the operation
     * @return the id of the stored data set
     */
    fun storeDataset(
        uploadedDataSet: StorableDataSet,
        bypassQa: Boolean,
        correlationId: String,
    ): String

    /**
     * Retrieves the content of a dataset from the backend storage engien
     * @param datasetId the id of the data set
     * @param dataType the type of data set
     * @param correlationId the correlation id for the operation
     * @return the data set in form of a JSON string
     */
    fun getDatasetData(
        datasetId: String,
        dataType: String,
        correlationId: String,
    ): String
}
