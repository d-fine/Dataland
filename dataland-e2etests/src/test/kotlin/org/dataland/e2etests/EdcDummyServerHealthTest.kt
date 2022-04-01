package org.dataland.e2etests

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class EdcDummyServerHealthTest {
    private val basePathToDatalandProxy = "http://proxy:80/api"

    @Test
    fun `test if EDC Dummy Server is up by using the backend actuator health endpoint`() {
        val client = OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build()
        var responseString = "INITIALIZE"
        try {
            val request: Request = Request.Builder()
                .url("$basePathToDatalandProxy/actuator/health/edcDummyServer").build()
            val response: Response = client.newCall(request).execute()
            responseString = response.body?.string().toString()
        } catch (_: Exception) {
            println(
                "The EDC Dummy Server could not be checked via the backend health endpoint." +
                    " HTTP Request failed."
            )
        }
        Assertions.assertEquals(
            "{\"status\":\"UP\"}", responseString,
            "The EDC Dummy Server does not have the status \"UP\" when checking it via " +
                "the backend actuator health point. It responds \"$responseString.\""
        )
    }
}
