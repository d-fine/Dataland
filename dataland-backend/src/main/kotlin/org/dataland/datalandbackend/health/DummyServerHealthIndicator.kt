package org.dataland.datalandbackend.health

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

/**
 * This class customizes the actuator/health endpoint of the backend to display if the given server
 * is available.
 */
open class DummyServerHealthIndicator(private val basePath: String) : HealthIndicator {

    /**
     * A method to build the health status of the given server.
     * @return a Health object
     */
    override fun health(): Health {
        return if (isRunningSkyminderServer()) {
            Health.up().build()
        } else {
            Health.down().build()
        }
    }

    /**
     * A method to check the health status of the given server by sending a request to its health endpoint.
     * @return a Boolean that declares if the given server runs
     */
    fun isRunningSkyminderServer(): Boolean {
        val client = OkHttpClient()

        try {
            val request: Request = Request.Builder()
                .url(basePath).build()
            val response: Response = client.newCall(request).execute()

            return response.body?.string().toString().contains("UP")
        } catch (_: Exception) {
            Unit
        }
        return false
    }
}
