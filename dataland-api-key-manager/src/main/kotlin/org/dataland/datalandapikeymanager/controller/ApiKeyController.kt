package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(username: String?, expiryDate: String?): ResponseEntity<ApiKey> {
        return ResponseEntity.ok(ApiKey("testuser1234", "somewhere future", "5678"))
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
