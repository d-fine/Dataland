package org.dataland.keycloakAdapter.utils

import org.springframework.security.core.Authentication

fun Authentication.hasAuthority(authority: String): Boolean {
    return this.authorities.any { it.authority == authority }
}

fun Authentication.getUserId(): String {
    return this.name
}