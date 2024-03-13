package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class DataRequestAlterationManagerTest {
    private lateinit var dataRequestAlterationManager: DataRequestAlterationManager
    private lateinit var dataRequestedAnsweredEmailMessageSender: DataRequestedAnsweredEmailMessageSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var dataRequestRepository: DataRequestRepository
    private val dataRequestId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()
    private val dummyDataRequestEntity: DataRequestEntity = DataRequestEntity(
        userId = "",
        dataType = "",
        reportingPeriod = "",
        creationTimestamp = 0,
        datalandCompanyId = "",
    )

    @BeforeEach
    fun setupDataRequestAlterationManager() {
        dataRequestedAnsweredEmailMessageSender = mock(DataRequestedAnsweredEmailMessageSender::class.java)
        dataRequestRepository = mock(DataRequestRepository::class.java)
        `when`<Any>(
            dataRequestRepository.findById(dataRequestId),
        ).thenReturn(Optional.of(dummyDataRequestEntity))
        doNothing().`when`(dataRequestedAnsweredEmailMessageSender)
            .sendDataRequestedAnsweredEmail(dummyDataRequestEntity, correlationId)

        dataRequestAlterationManager = DataRequestAlterationManager(
            dataRequestRepository = dataRequestRepository,
            dataRequestLogger = mock(DataRequestLogger::class.java),
            dataRequestedAnsweredEmailMessageSender = dataRequestedAnsweredEmailMessageSender,
        )

        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "user@requests.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that a request answered email is send when a request status is patched to answered`() {
        dataRequestAlterationManager.patchDataRequestStatus(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Answered,
        )
        fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
        verify(dataRequestedAnsweredEmailMessageSender, times(1))
            .sendDataRequestedAnsweredEmail(any(DataRequestEntity::class.java), anyString())
    }

    @Test
    fun `validate that a request answered email is not send when a request status is patched to any but answered`() {
        for (requestStatus in RequestStatus.entries) {
            if (requestStatus == RequestStatus.Answered) {
                continue
            }
            dataRequestAlterationManager.patchDataRequestStatus(
                dataRequestId = dataRequestId,
                requestStatus = requestStatus,
            )
        }
        verifyNoInteractions(dataRequestedAnsweredEmailMessageSender)
    }
}
