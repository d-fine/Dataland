package org.dataland.e2etests.tests

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MalformedRawRequestTests {
    private val client = OkHttpClient()
    private val tokenHandler = TokenHandler()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    @Test
    fun `sending a request to a non existent endpoint should yield a 404 response`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("this-endpoint-does-not-exist")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .get()
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        assertEquals(404, response.code)
    }

    @Test
    fun `sending a request with missing properties should result in a 400 response`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("companies")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .post("{}".toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body.string()
        assertTrue(responseBodyString.contains("Parameter specified as non-null is null"))
        assertEquals(400, response.code)
    }

    @Test
    fun `sending a request with additional non requested json properties should result in a 400 response`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("data")
            .addPathSegment("eutaxonomy-non-financials")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .post("{\"companyId\": \"doesntexist\",\"data\": {\"heyo\": \"Hey\"}}".toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body.string()
        assertTrue(responseBodyString.contains("Unrecognized field"))
        assertEquals(400, response.code)
    }

    @Test
    fun `sending a request with a non implemented request method results in a 405 error`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("companies")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .put(EMPTY_REQUEST)
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        assertEquals(405, response.code)
    }

    @Test
    fun `sending a funny request results in a 400 error`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("companies")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .method("LINK", EMPTY_REQUEST)
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body.string()
        assertTrue(responseBodyString.contains("request-rejected"))
        assertEquals(400, response.code)
    }

    @Test
    fun `test that the stacktrace is present in the local development environment but not in the production build`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("this-endpoint-does-not-exist")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .get()
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body.string()
        val containsStackTrace = responseBodyString.contains("\"stackTrace\"")
        val shouldContainStackTrace = (System.getenv("EXPECT_STACKTRACE") ?: "false") == "true"
        assertEquals(shouldContainStackTrace, containsStackTrace)
        assertEquals(404, response.code)
    }

    @Test
    fun `sending a request with a malformed dataType should result in a 400 error`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("companies")
            .addQueryParameter("dataTypes", "this-datatype-does-not-exist")
            .build()
        val request = Request.Builder()
            .url(endpointUrl)
            .get()
            .addHeader("Authorization", "Bearer ${ApiClient.accessToken}")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body.string()
        assertTrue(responseBodyString.contains("invalid-input"))
        assertEquals(400, response.code)
    }
}
