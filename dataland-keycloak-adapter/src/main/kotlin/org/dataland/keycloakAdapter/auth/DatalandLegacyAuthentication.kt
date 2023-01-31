package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * A wrapper around spring boots authentication object that exposes
 * commonly required user information in an easy-to-user manner
 */
class DatalandLegacyAuthentication(val authentication: Authentication) {
    companion object {
        /**
         * Builds a new DatalandAuthentication object from the Spring Boot Authentication
         * linked to the current thread. Throws an exception if no authentication data can be retrieved
         */
        fun fromContext(): DatalandLegacyAuthentication {
            val auth = fromContextOrNull()
            requireNotNull(auth)
            return auth
        }

        /**
         * Builds a new DatalandAuthentication object from the Spring Boot Authentication
         * linked to the current thread
         */
        fun fromContextOrNull(): DatalandLegacyAuthentication? {
            val auth = SecurityContextHolder.getContext().authentication
            return if (auth == null) null
            else DatalandLegacyAuthentication(auth)
        }
    }

    val username: String
        get() {
            if (authentication.principal is Jwt) {
                val jwt: Jwt = authentication.principal as Jwt
                return jwt.getClaimAsString("preferred_username")
            } else {
                throw UnsupportedOperationException("Cannot get the username of a user logged in via API key")
            }
        }

    val userId: String = authentication.name
    val roles: Set<DatalandRealmRole> = computeRealmRoles()

    private fun computeRealmRoles(): Set<DatalandRealmRole> {
        val allAuthorities = authentication.authorities.map { it.toString() }.toSet()
        return DatalandRealmRole.values().filter { allAuthorities.contains(it.toString()) }.toSet()
    }
}
