package org.dataland.datalandinternalstorage.services

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
class StreamingTemporarilyCachedDocumentControllerApi(
    @Value("\${dataland.document-manager.base-url}")
    private val documentManagerBaseUrl: String,
) {

    private val client = OkHttpClient()

    /**
     * Retrieves the blob identified by the blobId from the document manager and returns the input stream of the
     * body of the request.
     * @param blobId the id of the data to retrieve
     * @returns a stream of the blob
     */
    fun getReceivedData(blobId: String): InputStream {
        val requestUrl = "$documentManagerBaseUrl/internal/cached".toHttpUrl()
            .newBuilder().addPathSegment(blobId)
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
