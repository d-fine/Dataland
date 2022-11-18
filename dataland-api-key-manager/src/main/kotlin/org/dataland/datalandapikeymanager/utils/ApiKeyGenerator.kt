package org.dataland.datalandapikeymanager.utils

import org.dataland.datalandapikeymanager.model.ApiKey
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import java.security.SecureRandom
import java.time.LocalDate
import java.util.HexFormat
import javax.servlet.http.HttpServletRequest

class ApiKeyGenerator {

    private val keyByteLength = 40

    private fun generateApiKey(): String {
        val bytes = ByteArray(keyByteLength)
        SecureRandom().nextBytes(bytes)
        return HexFormat.of().formatHex(bytes)
    }

    fun getNewApiKey(daysValid: Long?, request: HttpServletRequest): ApiKey {
        val expiryDate: LocalDate? = if (daysValid == null) null else LocalDate.now().plusDays(daysValid)
        return ApiKey(getKeycloakUsername(request), expiryDate, generateApiKey())
    }

    private fun getKeycloakUsername(request: HttpServletRequest): String {
        //return "NOT IMPLEMENTED"
        val authenticationToken = request.userPrincipal as KeycloakAuthenticationToken
        return authenticationToken.account.keycloakSecurityContext.idToken.subject
    }

}