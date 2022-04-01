package org.dataland.e2etests

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class SkyminderDummyServerHealthTest {
    private val basePathToDatalandProxy = "http://proxy:80/api"

    @Test
    fun `test if Skyminder Dummy Server is up by using the backend actuator health endpoint`() {
        val client = OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build()
        var responseString = "INITIALIZE"
        try {
            val request: Request = Request.Builder()
                .url("$basePathToDatalandProxy/actuator/health/skyminderDummyServer").build()
            val response: Response = client.newCall(request).execute()
            responseString = response.body?.string().toString()
        } catch (_: Exception) {
            println(
                "The Skyminder Dummy Server could not be checked via the backend health endpoint." +
                    " HTTP Request failed."
            )
        }
        assertEquals(
            "{\"status\":\"UP\"}", responseString,
            "The Skyminder Dummy Server does not have the status \"UP\" when checking it via " +
                "the backend actuator health point. It responds \"$responseString.\""
        )
    }
}
