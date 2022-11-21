package org.dataland.datalandbackend.apikey

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component

@Component
class ApiKeyAuthenticationManager : AuthenticationManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun authenticate(authentication: Authentication?): Authentication {
        val customToken = authentication!!.principal as String
        logger.info("AKAM: Token: $customToken")
        return PreAuthenticatedAuthenticationToken("AuthenticatedUser", "N/A", mutableListOf(GrantedAuthority { "ROLE_ADMIN" }, GrantedAuthority { "ROLE_USER" }))
    }
}
