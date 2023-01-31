package org.dataland.datalandbackend.utils

import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.mockito.Mockito
import org.springframework.security.oauth2.jwt.Jwt

object AuthenticationMock {
    fun mockJwtAuthentication(
        username: String,
        userId: String,
        roles: Set<DatalandRealmRole>
    ): DatalandJwtAuthentication {
        val jwt = Mockito.mock(Jwt::class.java)
        Mockito.`when`(jwt.getClaimAsString("preferred_username")).thenReturn(username)
        val roleMap = mapOf("roles" to roles.map { it.toString() })
        Mockito.`when`(jwt.getClaimAsMap("realm_access")).thenReturn(roleMap)
        Mockito.`when`(jwt.subject).thenReturn(userId)
        val auth = DatalandJwtAuthentication(jwt)
        auth.isAuthenticated = true
        return auth
    }
}
