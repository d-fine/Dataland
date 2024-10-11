package org.dataland.datalandexternalstorage.services

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream

/**
 * An interface to the temporary storage of the document management service
 * that allows raw access to the body data-stream
 */
@Component
class StreamingTemporarilyCachedPrivateDocumentControllerApi(
    @Value("\${dataland.backend.base-url}")
    private val backendBaseUrl: String,
) {
    private val client = OkHttpClient()

    /**
     * Retrieves the blob identified by the blobId from the backend and returns the input stream of the
     * body of the request.
     * @param blobId the ID of the data to retrieve
     * @returns a stream of the blob
     */
    fun getReceivedPrivateDocument(blobId: String): InputStream {
        val requestUrl =
            "$backendBaseUrl/internal/cached/private/document"
                .toHttpUrl()
                .newBuilder()
                .addPathSegment(blobId)
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
