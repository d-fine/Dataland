package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * An Authentication class that instantiates a DatalandAuthentication out of a given payload.
 */
class DatalandInternalAuthentication(
    override val userId: String,
    userRoles: Set<DatalandRealmRole>,
) : DatalandAuthentication() {
    private val grantedAuthorities: List<GrantedAuthority> =
        userRoles.map { SimpleGrantedAuthority(it.toString()) }

    override fun getAuthorities(): List<GrantedAuthority> = grantedAuthorities

    override fun getCredentials(): Any = ""
}
