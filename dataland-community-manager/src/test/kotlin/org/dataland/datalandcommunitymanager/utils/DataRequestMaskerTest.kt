package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class DataRequestMaskerTest {
    private lateinit var authenticationMock: DatalandJwtAuthentication
    val dataRequestMasker = DataRequestMasker()
    private val userId = "1234-221-1111elf"

    private val dataRequestEntity: DataRequestEntity =
        DataRequestEntity(
            userId = "",
            dataType = "p2p",
            reportingPeriod = "",
            creationTimestamp = 0,
            datalandCompanyId = "",
        )

    private val extendedStoredDataRequest: ExtendedStoredDataRequest =
        ExtendedStoredDataRequest(
            dataRequestEntity = dataRequestEntity,
            companyName = "",
            userEmailAddress = "",
        )

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_USER),
            )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validates that normal user is not an admin`() {
        assertFalse(dataRequestMasker.isUserAdmin())
    }

    @Test
    fun `validates that adminComment are null for non admins`() {
        val modifiedExtendedStoredDataRequest = extendedStoredDataRequest.copy(adminComment = "test")
        val modifiedDataRequestEntityList =
            dataRequestMasker.hideAdminCommentForNonAdmins(
                listOf(modifiedExtendedStoredDataRequest),
            )
        val allAdminCommentsNull = modifiedDataRequestEntityList.all { it.adminComment == null }
        assertTrue(allAdminCommentsNull)
    }
}
