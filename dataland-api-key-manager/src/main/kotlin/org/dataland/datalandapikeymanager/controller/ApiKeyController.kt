package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.dataland.datalandapikeymanager.services.ApiKeyManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController(
    @Autowired private val apiKeyManager: ApiKeyManager,
) : ApiKeyAPI {

    override fun generateApiKey(daysValid: Int?): ResponseEntity<ApiKeyAndMetaInfo> {
        return ResponseEntity.ok(apiKeyManager.generateNewApiKey(daysValid))
    }

    override fun validateApiKey(apiKey: String): ResponseEntity<ApiKeyMetaInfo> {
        return ResponseEntity.ok(apiKeyManager.validateApiKey(apiKey))
    }

    override fun revokeApiKey(): ResponseEntity<RevokeApiKeyResponse> {
        return ResponseEntity.ok(apiKeyManager.revokeApiKey())
    }
}
