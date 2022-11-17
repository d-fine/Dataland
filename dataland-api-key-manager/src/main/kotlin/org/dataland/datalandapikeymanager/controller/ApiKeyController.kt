package org.dataland.datalandapikeymanager.controller

import org.apache.catalina.User
import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

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
        val authentication = SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User
        return authentication.username
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
