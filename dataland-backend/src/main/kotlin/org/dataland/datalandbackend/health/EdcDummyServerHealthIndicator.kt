package org.dataland.datalandbackend.health

import org.springframework.stereotype.Component

@Component
class EdcDummyServerHealthIndicator : DummyServerHealthIndicator("http://edc-dummyserver:8080/actuator/health")