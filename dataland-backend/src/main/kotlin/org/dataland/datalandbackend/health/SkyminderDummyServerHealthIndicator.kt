package org.dataland.datalandbackend.health

import org.springframework.stereotype.Component

/**
 * This class customizes the actuator/health endpoint of the backend to display if the Skyminder-Dummy-Server
 * is available.
 */
@Component
class SkyminderDummyServerHealthIndicator : HealthIndicatorExtensionForDummyServers("http://skyminder-server:8080/actuator/health")
