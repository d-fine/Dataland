package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class DatalandInternalAuthentication(
    override val userId: String,
    private val token: String = "internal",
    private val grantedAuthorities: Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_PREMIUM_USER")),
) : DatalandAuthentication() {
    override fun getCredentials(): Any = token

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities
}
