package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param companyManager the company manager service to handle company information
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(username: String?, expiryDate: String?): ResponseEntity<ApiKey> {
        return ResponseEntity.ok(ApiKey("testuser1234", "somewhere future", "5678"))
    }
}
