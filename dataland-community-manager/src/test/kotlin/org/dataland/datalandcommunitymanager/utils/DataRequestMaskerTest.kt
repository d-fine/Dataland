package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

/**
 * Tests the masking of admin comments
 * */
class DataRequestMaskerTest {
    private lateinit var dataRequestMasker: DataRequestMasker
    private lateinit var mockAuthentication: DatalandJwtAuthentication
    private lateinit var mockKeycloakUserService: KeycloakUserService
    private val userId = "1234-221-1111elf"
    private val testComment = "test comment"

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
            requestPriority = RequestPriority.Low,
            adminComment = testComment,
            messageHistory = listOf(),
            dataRequestStatusHistory = listOf(),
        )

    private val keycloakUserAlpha =
        KeycloakUserInfo(
            email = "alpha@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Michael",
            lastName = "Smith",
        )

    private fun setupMocks() {
        mockKeycloakUserService = mock(KeycloakUserService::class.java)
        `when`(
            mockKeycloakUserService.getUser(keycloakUserAlpha.userId),
        ).thenReturn(keycloakUserAlpha)
    }

    @BeforeEach
    fun setupDataRequestMasker() {
        setupMocks()
        dataRequestMasker = DataRequestMasker(mockKeycloakUserService)
    }

    private fun getExtendedStoredDataRequestEntityWithAdminComment(): ExtendedStoredDataRequest {
        val dummyDataRequestEntity =
            DataRequestEntity(
                userId = "",
                dataType = "p2p",
                emailOnUpdate = false,
                reportingPeriod = "",
                creationTimestamp = 0,
                datalandCompanyId = "",
            )
        dummyDataRequestEntity.adminComment = testComment
        val extendedStoredDataRequestWithAdminComment =
            ExtendedStoredDataRequest(
                dataRequestEntity = dummyDataRequestEntity,
                companyName = "",
                userEmailAddress = "",
            )
        return extendedStoredDataRequestWithAdminComment
    }

    private fun setupAdminAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_ADMIN),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun setupNonAdminAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validates that admin comments are not visible to non admins for a list of ExtendedStoredDataRequests`() {
        setupNonAdminAuthentication()
        val modifiedDataRequestEntityList =
            dataRequestMasker.hideAdminCommentForNonAdmins(
                listOf(getExtendedStoredDataRequestEntityWithAdminComment()),
            )
        val allAdminCommentsNull = modifiedDataRequestEntityList.all { it.adminComment == null }
        assertTrue(allAdminCommentsNull)
    }

    @Test
    fun `validates that admin comments are visible to admins for a list of ExtendedStoredDataRequests`() {
        setupAdminAuthentication()

        val modifiedDataRequestEntityList =
            dataRequestMasker.hideAdminCommentForNonAdmins(
                listOf(getExtendedStoredDataRequestEntityWithAdminComment()),
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
