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
    override fun generateApiKey(daysValid: Int?): ResponseEntity<ApiKeyAndMetaInfo> =
        ResponseEntity.ok(apiKeyManager.generateNewApiKey(daysValid))

    override fun getApiKeyMetaInfoForUser(): ResponseEntity<ApiKeyMetaInfo> =
        ResponseEntity
            .ok(apiKeyManager.getApiKeyMetaInfoForFrontendUser())

    override fun validateApiKey(apiKey: String): ResponseEntity<ApiKeyMetaInfo> = ResponseEntity.ok(apiKeyManager.validateApiKey(apiKey))

    override fun revokeApiKey(): ResponseEntity<RevokeApiKeyResponse> = ResponseEntity.ok(apiKeyManager.revokeApiKey())
}
