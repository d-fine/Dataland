package org.dataland.datalandinternalstorage.services

import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream

/**
 * The class holds the method to retrieve a document from the document-manager
 */
@Component
class StreamingTemporarilyCachedDocumentControllerApi(
    @Value("\${dataland.document-manager.base-url}")
    private val documentManagerBaseUrl: String,
) {

    private val client = OkHttpClient()

    /**
     * The method retrieves a document temporary stored in the document-manager
     * @param blobId the hash of the document
     */
    fun getReceivedData(blobId: String): InputStream {
        val request = Request.Builder().url("$documentManagerBaseUrl/internal/cached/$blobId").build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw InternalServerErrorApiException(
                "API call to temporarily cached document controller failed " +
                    "(code ${response.code}).",
            )
        }
        return response.body!!.byteStream()
    }
}
