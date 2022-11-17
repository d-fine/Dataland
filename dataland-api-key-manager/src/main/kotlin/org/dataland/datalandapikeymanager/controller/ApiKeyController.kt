package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(daysValid: Long?): ResponseEntity<ApiKey> {
        val expiryDate: LocalDate? = if (daysValid == null) null else LocalDate.now().plusDays(daysValid)
        return ResponseEntity.ok(ApiKey("testuser1234", expiryDate, "5678"))
    }
}
