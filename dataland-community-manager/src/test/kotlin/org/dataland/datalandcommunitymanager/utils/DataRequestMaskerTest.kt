package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class DataRequestMaskerTest {
    private lateinit var authenticationMock: DatalandJwtAuthentication
    val dataRequestMasker = DataRequestMasker()
    private val userId = "1234-221-1111elf"
    private val testComment = "test comment"

    private val dummyDataRequestEntity =
        DataRequestEntity(
            userId = "",
            dataType = "p2p",
            reportingPeriod = "",
            creationTimestamp = 0,
            datalandCompanyId = "",
        )

    private val storedDataRequestEntityWithAdminComment =
        StoredDataRequest(
            dataRequestId = "",
            userId = "",
            userEmailAddress = "",
            creationTimestamp = 0,
            dataType = "p2p",
            reportingPeriod = "",
            datalandCompanyId = "",
            lastModifiedDate = 0,
            requestStatus = RequestStatus.Open,
            accessStatus = AccessStatus.Pending,
            requestPriority = RequestPriority.Normal,
            adminComment = testComment,
            messageHistory = listOf(),
            dataRequestStatusHistory = listOf(),
        )

    private fun getExtendedStoredDataRequestEntityWithAdminCommentList(): List<ExtendedStoredDataRequest> {
        val dataRequestEntityWithAdminComment = dummyDataRequestEntity.copy(adminComment = testComment)
        val extendedStoredDataRequestWithAdminComment =
            ExtendedStoredDataRequest(
                dataRequestEntity = dataRequestEntityWithAdminComment,
                companyName = "",
                userEmailAddress = "",
            )
        return listOf(extendedStoredDataRequestWithAdminComment)
    }

    private fun setupAdminAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_ADMIN),
            )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun setupNonAdminAuthentication() {
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
    fun `validates that a normal user is not recognized as an admin`() {
        setupNonAdminAuthentication()
        assertFalse(dataRequestMasker.isUserAdmin())
    }

    @Test
    fun `validates that admins are recognized as admin`() {
        setupAdminAuthentication()
        assertTrue(dataRequestMasker.isUserAdmin())
    }

    @Test
    fun `validates that admin comments are not visible to non admins for a list of ExtendedStoredDataRequests`() {
        setupNonAdminAuthentication()
        val modifiedDataRequestEntityList =
            dataRequestMasker.hideAdminCommentForNonAdmins(
                getExtendedStoredDataRequestEntityWithAdminCommentList(),
            )
        val allAdminCommentsNull = modifiedDataRequestEntityList.all { it.adminComment == null }
        assertTrue(allAdminCommentsNull)
    }

    @Test
    fun `validates that admin comments are visible to admins for a list of ExtendedStoredDataRequests`() {
        setupAdminAuthentication()

        val modifiedDataRequestEntityList =
            dataRequestMasker.hideAdminCommentForNonAdmins(
                getExtendedStoredDataRequestEntityWithAdminCommentList(),
            )
        val extendedStoredDataRequest = modifiedDataRequestEntityList[0]

        assertEquals(testComment, extendedStoredDataRequest.adminComment)
    }

    @Test
    fun `validates that admin comments are not visible to non admins for single StoredDataRequest`() {
        setupNonAdminAuthentication()
        val modifiedStoredDataRequest =
            dataRequestMasker.hideAdminCommentForNonAdmins(storedDataRequestEntityWithAdminComment)
        assertNull(modifiedStoredDataRequest.adminComment)
    }

    @Test
    fun `validates that admin comments are visible to admins for single StoredDataRequest`() {
        setupAdminAuthentication()
        val modifiedStoredDataRequest =
            dataRequestMasker.hideAdminCommentForNonAdmins(storedDataRequestEntityWithAdminComment)
        assertEquals(testComment, modifiedStoredDataRequest.adminComment)
    }
}
