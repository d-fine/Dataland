package org.dataland.datalandbackend.health

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * This class customizes the actuator/health endpoint of the backend to display if the Skyminder-Dummy-Server
 * is available.
 */
@Component
class SkyminderDummyServerHealthIndicator : HealthIndicator {

    /**
     * A method to build the health status of the Skyminder-Dummy-Server.
     * @return a Health object
     */
    override fun health(): Health {
        return if (isRunningSkyminderServer()) { Health.up().build() } else { Health.down().build() }
    }

    /**
     * A method to check the health status of the Skyminder-Dummy-Server by sending a request to its health endpoint.
     * @return a Boolean that declares if Skyminder-Dummy-Server runs
     */
    fun isRunningSkyminderServer(): Boolean {
        val client = OkHttpClient()

        try {
            val request: Request = Request.Builder()
                .url("http://skyminder-server:8080/actuator/health").build()
            val response: Response = client.newCall(request).execute()

            return response.body?.string().toString().contains("UP")
        } catch (_: Exception) {
            Unit
        }
        return false
    }
}
