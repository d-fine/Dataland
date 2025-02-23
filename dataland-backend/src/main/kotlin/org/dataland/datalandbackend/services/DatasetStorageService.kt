package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackendutils.model.BasicDataDimensions

/**
 * Service to store and retrieve datasets from a backing storage engine
 */
interface DatasetStorageService {
    /**
     * Stores a dataset in the backing storage engine
     * @param uploadedDataset the dataset to process
     * @param bypassQa whether to bypass the QA process
     * @param correlationId the correlation id for the operation
     * @return the id of the stored dataset
     */
    fun storeDataset(
        uploadedDataset: StorableDataset,
        bypassQa: Boolean,
        correlationId: String,
    ): String

    /**
     * Retrieves the content of a dataset from the backend storage engine given the dataset ID
     * @param datasetId the id of the dataset
     * @param dataType the type of dataset
     * @param correlationId the correlation id for the operation
     * @return the dataset in form of a JSON string
     */
    fun getDatasetData(
        datasetId: String,
        dataType: String,
        correlationId: String,
    ): String

    /**
     * Retrieves the content of a dataset from the backend storage engine given the data dimensions
     * @param dataDimensions the dimensions of the dataset to be retrieved
     * @param correlationId the correlation id for the operation
     * @return the dataset in form of a JSON string
     */
    fun getDatasetData(
        dataDimensions: BasicDataDimensions,
        correlationId: String,
    ): String?
}
