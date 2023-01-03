package org.dataland.datalandbackend.utils

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * A class that obtains some keycloak user properties of the current request's authenticated user
 */
class KeycloakUserUtils {
    companion object {
        /**
         * A method to obtain the keycloak user id of the current authenticated request
         * @return the keycloak user id of the current authenticated request
         */
        fun getUserIdFromSecurityContext(): String {
            return SecurityContextHolder.getContext().authentication.name
        }

        /**
         * A method to obtain the keycloak username of the current authenticated request
         * @return the keycloak username of the current authenticated request
         */
        fun getUsernameFromSecurityContext(): String {
            val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
            return jwt.getClaimAsString("preferred_username")
        }
    }
}