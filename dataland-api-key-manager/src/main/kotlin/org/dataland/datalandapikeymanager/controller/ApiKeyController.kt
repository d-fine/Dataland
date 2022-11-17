package org.dataland.datalandapikeymanager.controller

import org.dataland.datalandapikeymanager.api.ApiKeyAPI
import org.dataland.datalandapikeymanager.model.ApiKey
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

/**
 * Controller for the api key manager
 */

@RestController
class ApiKeyController : ApiKeyAPI {
    override fun generateApiKey(daysValid: Long?, request: ServerHttpRequest): ResponseEntity<ApiKey> {
        val expiryDate: LocalDate? = if (daysValid == null) null else LocalDate.now().plusDays(daysValid)
        return ResponseEntity.ok(ApiKey(getKeycloakUsername(request), expiryDate, "5678"))
    }

    private fun getKeycloakUsername(request: ServerHttpRequest): String {
        // TODO this is not working yet
        val authenticationToken = request.principal as KeycloakAuthenticationToken
        return authenticationToken.account.keycloakSecurityContext.idToken.subject
    }

    override fun validateApiKey(apiKey: String?): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}
