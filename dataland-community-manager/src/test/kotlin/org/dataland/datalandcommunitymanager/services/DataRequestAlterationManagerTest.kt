package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.*

class DataRequestAlterationManagerTest {
    private lateinit var dataRequestAlterationManager: DataRequestAlterationManager
    private lateinit var dataRequestResponseEmailMessageSender: DataRequestResponseEmailSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var dataRequestRepository: DataRequestRepository
    private lateinit var singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender
    private lateinit var metaDataControllerApi: MetaDataControllerApi
    private lateinit var messageRepository: MessageRepository
    private val dataRequestId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()
    private val dummyDataRequestEntities: List<DataRequestEntity> = listOf(
        DataRequestEntity(
            userId = "",
            dataType = "p2p",
            reportingPeriod = "",
            creationTimestamp = 0,
            datalandCompanyId = "",
        ),
        DataRequestEntity(
            userId = "dummyId",
            dataType = "dummyDataType",
            reportingPeriod = "dummyPeriod",
            creationTimestamp = 123456,
            datalandCompanyId = "dummyCompanyId",
        ),
    )
    private val dummyDataRequestEntity: DataRequestEntity = dummyDataRequestEntities[0]
    private val metaData = DataMetaInformation(
        dataId = UUID.randomUUID().toString(),
        companyId = "companyId",
        dataType = DataTypeEnum.p2p,
        uploadTime = 0,
        reportingPeriod = "",
        currentlyActive = false,
        qaStatus = QaStatus.Accepted,
    )
    private val dummyMessage = StoredDataRequestMessageObject(
        contacts = setOf("test@example.com"),
        message = "test message",
        creationTimestamp = Instant.now().toEpochMilli(),
    )
    private fun mockRepos() {
        dataRequestRepository = mock(DataRequestRepository::class.java)
        `when`<Any>(
            dataRequestRepository.findById(dataRequestId),
        ).thenReturn(Optional.of(dummyDataRequestEntity))
        `when`(
            dataRequestRepository.searchDataRequestEntity(
                searchFilter = GetDataRequestsSearchFilter(
                    metaData.dataType.value, "",
                    RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
                ),
            ),
        ).thenReturn(dummyDataRequestEntities)
        doNothing().`when`(dataRequestRepository).updateDataRequestEntitiesFromOpenToAnswered(
            metaData.companyId, metaData.reportingPeriod, metaData.dataType.value,
        )
        messageRepository = mock(MessageRepository::class.java)
        `when`(messageRepository.saveAllAndFlush(anyList())).thenReturn(
            emptyList(),
        )
    }

    @BeforeEach
    fun setupDataRequestAlterationManager() {
        mockRepos()
        singleDataRequestEmailMessageSender = mock(SingleDataRequestEmailMessageSender::class.java)
        doNothing().`when`(singleDataRequestEmailMessageSender)
            .sendSingleDataRequestExternalMessage(
                any(SingleDataRequestEmailMessageSender.MessageInformation::class.java),
                anyString(), anyString(), anyString(),
            )
        metaDataControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)
        dataRequestResponseEmailMessageSender = mock(DataRequestResponseEmailSender::class.java)

        doNothing().`when`(dataRequestResponseEmailMessageSender)
            .sendDataRequestResponseEmail(
                dummyDataRequestEntity, TemplateEmailMessage.Type.DataRequestedAnswered, correlationId,
            )

        dataRequestAlterationManager = DataRequestAlterationManager(
            dataRequestRepository = dataRequestRepository,
            dataRequestLogger = mock(DataRequestLogger::class.java),
            dataRequestResponseEmailMessageSender = dataRequestResponseEmailMessageSender,
            metaDataControllerApi = metaDataControllerApi,
            singleDataRequestEmailMessageSender = singleDataRequestEmailMessageSender,
            messageRepository = messageRepository,
        )
    }

    @BeforeEach
    fun setupSecurityMock() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "user@example.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that a request response email is send when a request status is patched to answered or closed`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Answered,
            null,
        )
        fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
        verify(dataRequestResponseEmailMessageSender, times(1))
            .sendDataRequestResponseEmail(
                any(DataRequestEntity::class.java), any(TemplateEmailMessage.Type::class.java), anyString(),
            )
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Closed,
            null,
        )
        verify(dataRequestResponseEmailMessageSender, times(2))
            .sendDataRequestResponseEmail(
                any(DataRequestEntity::class.java), any(TemplateEmailMessage.Type::class.java), anyString(),
            )
    }

    @Test
    fun `validate that a response email is not send when a request status is patched to any but answered or closed`() {
        for (requestStatus in RequestStatus.entries) {
            if (requestStatus == RequestStatus.Answered || requestStatus == RequestStatus.Closed) {
                continue
            }
            dataRequestAlterationManager.patchDataRequest(
                dataRequestId = dataRequestId,
                requestStatus = requestStatus,
                null,
            )
        }
        verifyNoInteractions(dataRequestResponseEmailMessageSender)
    }

    @Test
    fun `validate that an request answered email is send when request statuses are patched from open to answered`() {
        dataRequestAlterationManager.patchRequestStatusFromOpenToAnsweredByDataId(metaData.dataId, correlationId)
        dummyDataRequestEntities.forEach {
            verify(dataRequestResponseEmailMessageSender)
                .sendDataRequestResponseEmail(it, TemplateEmailMessage.Type.DataRequestedAnswered, correlationId)
        }
    }

    @Test
    fun `validate that the sending of a request email is triggered when a request message is added`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = null,
            dummyMessage.contacts,
            dummyMessage.message,
        )

        verify(singleDataRequestEmailMessageSender, times(1))
            .sendSingleDataRequestExternalMessage(
                any(SingleDataRequestEmailMessageSender.MessageInformation::class.java),
                anyString(), anyString(), anyString(),
            )
    }
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
