package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * A wrapper around spring boots authentication object that exposes
 * commonly required user information in an easy-to-user manner
 */
class DatalandAuthentication(val authentication: Authentication) {
    companion object {
        /**
         * Builds a new DatalandAuthentication object from the Spring Boot Authentication
         * linked to the current thread
         */
        fun fromContext(): DatalandAuthentication {
            return DatalandAuthentication(SecurityContextHolder.getContext().authentication)
        }
    }

    val jwt: Jwt = authentication.principal as Jwt
    val userId: String = authentication.name
    val username: String = jwt.getClaimAsString("preferred_username")
    val roles: Set<DatalandRealmRoles> = computeRealmRoles()

    private fun computeRealmRoles(): Set<DatalandRealmRoles> {
        val allAuthorities = authentication.authorities.map { it.toString() }.toSet()
        return DatalandRealmRoles.values().filter { allAuthorities.contains(it.toString()) }.toSet()
    }
}
