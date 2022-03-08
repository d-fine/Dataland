package org.dataland.e2etests

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderDummyServerHealthTest {

    @Test
    fun `test if Skyminder Dummy Server is running by using the backend actuator health endpoint`() {
        var running = false
        val client = OkHttpClient()
        var responseString = "INITIALIZE"

        try {
            val request: Request = Request.Builder()
                .url("http://proxy:80/api/actuator/health/skyminderDummyServer").build()
            val response: Response = client.newCall(request).execute()
            responseString = response.body?.string().toString()
            println(responseString)

            if (responseString.contains("UP")) {
                running = true
            }
        } catch (_: Exception) {
            println("The Skyminder Dummy Server could not be checked via the backend health endpoint.")
        }
        println(running)
        println(responseString)
        assertTrue(
            running,
            "The Skyminder Dummy Server does not have the status \"UP\" when checking it via " +
                "the backend actuator health point. It responds \"$responseString.\""
        )
    }
}
