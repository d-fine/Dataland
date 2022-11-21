package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.utils.ApiKeyGenerator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {

    val apiKeyGenerator = ApiKeyGenerator()

    override fun generateApiKey(daysValid: Int?): ResponseEntity<ApiKeyAndMetaInfo> {
        return ResponseEntity.ok(apiKeyGenerator.getNewApiKey(daysValid))
    }

    override fun validateApiKey(username: String, apiKey: String): ResponseEntity<ApiKeyMetaInfo> {
        return ResponseEntity.ok(apiKeyGenerator.validateApiKey(username, apiKey))
    }
}
