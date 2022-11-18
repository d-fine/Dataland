package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.dataland.datalandapikeymanager.utils.ApiKeyGenerator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(daysValid: Long?, request: HttpServletRequest): ResponseEntity<ApiKey> {
        return ResponseEntity.ok(ApiKeyGenerator().getNewApiKey(daysValid, request))
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
