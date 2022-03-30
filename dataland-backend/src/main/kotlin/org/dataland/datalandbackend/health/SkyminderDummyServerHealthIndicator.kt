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
class SkyminderDummyServerHealthIndicator : DummyServerHealthIndicator("http://skyminder-server:8080/actuator/health")
