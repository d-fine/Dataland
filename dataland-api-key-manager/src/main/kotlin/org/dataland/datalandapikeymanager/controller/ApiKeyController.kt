package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKeyData
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(daysValid: Long?): ResponseEntity<ApiKeyData> {
        val username = SecurityContextHolder.getContext().authentication.principal.toString()
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid)
        return ResponseEntity.ok(ApiKeyData(username, expiryDate, "5678"))
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
