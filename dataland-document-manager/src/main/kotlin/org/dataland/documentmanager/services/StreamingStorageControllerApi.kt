package org.dataland.documentmanager.services

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream

/**
 * An interface to the document endpoint of the internal storage service
 * that allows raw access to the body data-stream
 */
@Component
class StreamingStorageControllerApi(
    @Value("\${dataland.internalstorage.base-url}")
    private val internalStorageBaseUrl: String,
) {
    private val client = OkHttpClient()

    /**
     * Retrieves the blob identified by the blobId from the internal storage and returns the input stream of the
     * body of the request.
     * @param blobId the id of the data to retrieve
     * @param correlationId an identifier used to identify the transaction across services
     * @returns a stream of the blob
     */
    fun getBlobFromInternalStorage(blobId: String, correlationId: String): InputStream {
        val requestUrl = "$internalStorageBaseUrl/blobs".toHttpUrl()
            .newBuilder().addPathSegment(blobId).addQueryParameter("correlationId", correlationId)
            .build()
        val request = Request.Builder().url(requestUrl).build()
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
