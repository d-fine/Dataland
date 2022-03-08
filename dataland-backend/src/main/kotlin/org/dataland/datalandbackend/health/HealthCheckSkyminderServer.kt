package org.dataland.datalandbackend.health

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component("HealthCheckerSkyminder")
class HealthCheckSkyminderServer : HealthIndicator {
    private val messageKey = "Skyminder-Server"

    override fun health(): Health {
        return if (!isRunningSkyminderServer()) {
            Health.down().withDetail(messageKey, "Not Available").build()
        } else Health.up().withDetail(messageKey, "Available").build()
    }

    fun isRunningSkyminderServer(): Boolean {

        val client = OkHttpClient()

        val request: Request =

            Request.Builder()
                .url("http://skyminder-server:8080/actuator/health")
                .build()

        val call: Call = client.newCall(request)
        val response: Response = call.execute()
        val responseString = response.body?.string()

        println(responseString)

        if (responseString != null) {
            if (responseString.contains("UP")) {
                return true
            }
        }
        return false
    }
}
