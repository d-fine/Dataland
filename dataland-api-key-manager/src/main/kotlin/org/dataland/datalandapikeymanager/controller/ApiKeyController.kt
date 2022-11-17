package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(daysValid: Long?): ResponseEntity<ApiKey> {
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid)
        return ResponseEntity.ok(ApiKey(getKeycloakUsername(), expiryDate, "5678"))
    }

    private fun getKeycloakUsername(): String {
        // TODO this is not working yet
        val authentication = SecurityContextHolder.getContext().authentication
        return if(authentication.isAuthenticated) authentication.name else "NOT AUTHENTICATED"
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
