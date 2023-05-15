package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * A base class for all dataland-related authentication objects (like API-Key / JWT authentication)
 */
sealed class DatalandAuthentication : Authentication {
    companion object {
        /**
         * Builds a new DatalandAuthentication object from the Spring Boot Authentication
         * linked to the current thread. Throws an exception if no authentication data can be retrieved
         */
        fun fromContext(): DatalandAuthentication {
            val auth = fromContextOrNull()
            requireNotNull(auth)
            return auth
        }

        /**
         * Builds a new DatalandAuthentication object from the Spring Boot Authentication
         * linked to the current thread
         */
        // @Suppress("SafeCast")
        fun fromContextOrNull(): DatalandAuthentication? {
            val auth = SecurityContextHolder.getContext().authentication
            return (auth as? DatalandAuthentication)
        }
    }

    abstract val userId: String

    val roles: Set<DatalandRealmRole>
        get() {
            val allUserRoles = authorities.map { it.toString() }
            return DatalandRealmRole.values().filter { allUserRoles.contains(it.toString()) }.toSet()
        }

    private var authenticated = false
    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

    override fun getDetails(): Any? = null
    override fun getPrincipal(): String = userId
    override fun getName(): String = userId
    override fun isAuthenticated() = authenticated
}
