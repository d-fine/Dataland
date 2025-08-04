package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Authentication class to rebuild DatalandAuthentication after the message queue for Bulk Data Request
 * triggered by Portfolio Monitoring.
 */
class DatalandInternalAuthentication(
    override val userId: String,
    private val token: String = "internal",
    private val grantedAuthorities: Collection<SimpleGrantedAuthority>,
) : DatalandAuthentication() {
    override fun getCredentials(): Any = token

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities
}
