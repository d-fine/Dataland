package org.dataland.datalandcommunitymanager.utils

import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

object TestUtils {
    /**
     * Setting up the security context for the specified dataland dummy user
     */
    fun mockSecurityContext(
        username: String,
        userId: String,
        roles: Set<DatalandRealmRole>,
    ): DatalandJwtAuthentication {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(username, userId, roles)
        `when`(mockAuthentication.credentials).thenReturn("mockAuthentication")
        `when`(mockAuthentication.credentials).thenReturn("")
        val mockSecurityContext = mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
        return mockAuthentication
    }

    /**
     * Setting up the security context for the specified dataland dummy user with fewer parameters
     */
    fun mockSecurityContext(
        username: String = "mocked_uploader",
        userId: String = "dummy-id",
        role: DatalandRealmRole = DatalandRealmRole.ROLE_PREMIUM_USER,
    ): DatalandJwtAuthentication = mockSecurityContext(username, userId, setOf(role))
}
