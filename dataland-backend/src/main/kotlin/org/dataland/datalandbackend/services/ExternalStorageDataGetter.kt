package org.dataland.datalandbackend.services

import org.dataland.datalandexternalstorage.openApiClient.api.ExternalStorageControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream

/**
 * The class holds the various storage clients for the data manager services
 */

@Service
class ExternalStorageDataGetter(
    @Autowired private val streamingStorageClient: StreamingExternalStorageControllerApi,
    @Autowired private val storageClient: ExternalStorageControllerApi,
) {
    /**
     * This method retrieves a document by using the external storage client and returns the document as an inputstream
     * @param eurodatId the eurodatId for the document to be retrieved
     * @param correlationId the correlationId of the request which caused the exception to be thrown
     */
    fun getBlobFromExternalStorage(
        eurodatId: String,
        correlationId: String,
    ): InputStream = streamingStorageClient.getBlobFromExternalStorage(eurodatId, correlationId)

    /**
     * This method retrieves a dataset by using the external storage client and returns the dataset as a string
     * @param dataId the dataId of the dataset to be retrieved
     * @param correlationId the correlationId of the corresponding process
     */
    fun getJsonFromExternalStorage(
        dataId: String,
        correlationId: String,
    ): String = storageClient.selectDataById(dataId, correlationId)
}
