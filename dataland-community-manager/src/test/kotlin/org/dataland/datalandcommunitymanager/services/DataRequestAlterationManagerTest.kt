package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anySet
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.*

class DataRequestAlterationManagerTest {

    private lateinit var dataRequestAlterationManager: DataRequestAlterationManager
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var dataRequestRepository: DataRequestRepository
    private lateinit var metaDataControllerApi: MetaDataControllerApi
    private lateinit var processingUtils: DataRequestProcessingUtils
    private lateinit var requestEmailManager: RequestEmailManager
    private lateinit var companyRolesManager: CompanyRolesManager

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
        dummyDataRequestEntities.forEach {
            `when`<Any>(
                dataRequestRepository.findById(it.dataRequestId),
            ).thenReturn(Optional.of(it))
        }
        `when`(
            dataRequestRepository.searchDataRequestEntity(
                searchFilter = DataRequestsFilter(
                    setOf(metaData.dataType), null, null, metaData.companyId, metaData.reportingPeriod,
                    setOf(RequestStatus.Open), null,
                ),
                prefetchedUserIdsByEmail = emptyList(),
            ),
        ).thenReturn(dummyDataRequestEntities)

        processingUtils = mock(DataRequestProcessingUtils::class.java)
        doNothing().`when`(processingUtils).addNewRequestStatusToHistory(
            any(), any(),
            any(), any(),
        )
        doNothing().`when`(processingUtils).addMessageToMessageHistory(
            any(), anySet(), anyString(), any(),
        )
    }

    @BeforeEach
    fun setupDataRequestAlterationManager() {
        mockRepos()

        requestEmailManager = mock(RequestEmailManager::class.java)
        companyRolesManager = mock(CompanyRolesManager::class.java)

        metaDataControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)

        dataRequestAlterationManager = DataRequestAlterationManager(
            dataRequestRepository = dataRequestRepository,
            dataRequestLogger = mock(DataRequestLogger::class.java),
            metaDataControllerApi = metaDataControllerApi,
            requestEmailManager = requestEmailManager,
            companyRolesManager = companyRolesManager,
            utils = processingUtils,
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
    fun `validate that a request response email is sent when a request status is patched to answered or closed`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Answered,
            null,
        )
        verify(requestEmailManager, times(1))
            .sendEmailsWhenStatusChanged(
                any(), eq(RequestStatus.Answered), eq(null), eq(null),
            )
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Closed,
            null,
        )
        verify(requestEmailManager, times(1))
            .sendEmailsWhenStatusChanged(
                any(), eq(RequestStatus.Closed), eq(null), eq(null),
            )
        verify(processingUtils, times(2))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any(),
            )
        verify(processingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that no email is sent and the history is updated when an access status is patched`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = null,
            accessStatus = AccessStatus.Pending,
        )

        verify(processingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any(),
            )

        verify(processingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that a request answered email is sent when request statuses are patched from open to answered`() {
        dataRequestAlterationManager.patchRequestStatusFromOpenToAnsweredByDataId(metaData.dataId, correlationId)
        dummyDataRequestEntities.forEach {
            verify(requestEmailManager)
                .sendEmailsWhenStatusChanged(eq(it), eq(RequestStatus.Answered), eq(null), anyString())
        }
        verify(processingUtils, times(dummyDataRequestEntities.size))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any(),
            )
        verify(processingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that the sending of a request email is triggered when a request message is added`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = null,
            accessStatus = null,
            dummyMessage.contacts,
            dummyMessage.message,
        )

        verify(requestEmailManager, times(1))
            .sendSingleDataRequestEmail(
                any(), anySet(), anyString(),
            )

        verify(processingUtils, times(1))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )

        verify(processingUtils, times(0))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any(),
            )
    }
}
