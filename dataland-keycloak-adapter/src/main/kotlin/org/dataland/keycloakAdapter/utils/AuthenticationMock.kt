package org.dataland.keycloakAdapter.utils

import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * This object holds a method to mock the authentication via JWT
 */
object AuthenticationMock {
    /**
     * This method mocks a JWT to authenticate for a given user
     * and executes a block of code with the mocked authentication
     */
    inline fun <T> withAuthenticationMock(
        username: String,
        userId: String,
        roles: Set<DatalandRealmRole>,
        tokenValue: String = "",
        block: () -> T,
    ): T {
        val auth = mockJwtAuthentication(username, userId, roles, tokenValue)
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(auth)
        val oldSecurityContext = SecurityContextHolder.getContext()
        SecurityContextHolder.setContext(mockSecurityContext)
        val returnValue = block()
        SecurityContextHolder.setContext(oldSecurityContext)
        return returnValue
    }

    /**
     * This method mocks a JWT to authenticate for a given user
     * @param username the username
     * @param userId the user Id
     * @param roles the roles of the user
     */
    fun mockJwtAuthentication(
        username: String,
        userId: String,
        roles: Set<DatalandRealmRole>,
        tokenValue: String = "",
    ): DatalandJwtAuthentication {
        val jwt = Mockito.mock(Jwt::class.java)
        `when`(jwt.getClaimAsString("preferred_username")).thenReturn(username)
        val roleMap = mapOf("roles" to roles.map { it.toString() })
        `when`(jwt.getClaimAsMap("realm_access")).thenReturn(roleMap)
        `when`(jwt.subject).thenReturn(userId)
        `when`(jwt.tokenValue).thenReturn(tokenValue)
        val auth = DatalandJwtAuthentication(jwt)
        auth.isAuthenticated = true
        return auth
    }

    /**
     * Mock the dataland security context. Especially useful for tests involving DatalandAuthentication.fromContext().
     */
    fun mockSecurityContext(
        username: String,
        userId: String,
        roles: Set<DatalandRealmRole>,
    ): DatalandJwtAuthentication {
        val mockAuthentication = mockJwtAuthentication(username, userId, roles)
        val mockSecurityContext = mock<SecurityContext>()
        doReturn(mockAuthentication).`when`(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
        return mockAuthentication
    }
}
