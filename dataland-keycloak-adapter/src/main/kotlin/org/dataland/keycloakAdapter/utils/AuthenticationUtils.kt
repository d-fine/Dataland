package org.dataland.keycloakAdapter.utils

import org.dataland.keycloakAdapter.DatalandRealmRoles
import org.springframework.security.core.Authentication

/**
 * Checks if a dataland user has a certain role.
 */
fun Authentication.hasRole(authority: DatalandRealmRoles): Boolean {
    return this.authorities.any { it.authority == authority.toString() }
}

/**
 * A more explicit alias to get the userId from the Authentication object
 */
fun Authentication.getUserId(): String {
    return this.name
}
