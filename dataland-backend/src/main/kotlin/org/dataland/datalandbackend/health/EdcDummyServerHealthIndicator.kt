package org.dataland.datalandbackend.health

import org.springframework.stereotype.Component

/**
 * This class customizes the actuator/health endpoint of the backend to display if the EDC-Dummy-Server
 * is available.
 */
@Component
class EdcDummyServerHealthIndicator : DummyServerHealthIndicator("http://edc-dummyserver:8080/actuator/health")
