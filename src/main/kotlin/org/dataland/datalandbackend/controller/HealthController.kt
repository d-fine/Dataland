package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.HealthAPI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController : HealthAPI {

    override fun getHealth(): ResponseEntity<String> {
        return ResponseEntity.ok("Healthy")
    }
}
