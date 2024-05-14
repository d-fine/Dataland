package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.services.StreamingExternalStorageControllerApi
import org.dataland.datalandexternalstorage.openApiClient.api.ExternalStorageControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.InputStream

/**
 * The class holds the various storage clients for the data manager services
 */

@Component("ExternalStorageClientUtils")
class ExternalStorageClientUtils(
    @Autowired private val streamingStorageClient: StreamingExternalStorageControllerApi,
    @Autowired private val storageClient: ExternalStorageControllerApi,
) {

    /**
     * his methods retrieves an document by using the external storage client and returns the document as an inputstream
     * @param eurodatId the documentId for the document to be retrieved
     * @param correlationId the correlationId of the request which caused the exception to be thrown
     */
    fun getBlobFromExternalStorage(eurodatId: String, correlationId: String): InputStream {
        return streamingStorageClient.getBlobFromExternalStorage(eurodatId, correlationId)
    }

    /**
     * This methods retrieves an dataset by using the external storage client and returns the dataset as a string
     * @param dataId the dataId of the dataset to be retrieved
     * @param correlationId the correlationId of the corresponding process
     */
    fun getJsonFromExternalStorage(
        dataId: String,
        correlationId: String,
    ): String {
        return storageClient.selectDataById(dataId, correlationId)
    }
}
