package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import java.util.Optional
import java.util.UUID

class DataRequestAlterationManagerTest {
    private lateinit var dataRequestAlterationManager: DataRequestAlterationManager
    private lateinit var nonSourceableDataManager: NonSourceableDataManager
    private lateinit var mockAuthentication: DatalandJwtAuthentication
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var mockMetaControllerApi: MetaDataControllerApi
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockRequestEmailManager: RequestEmailManager
    private lateinit var mockCompanyRolesManager: CompanyRolesManager

    private val dataRequestId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()
    private val dummyDataRequestEntities: List<DataRequestEntity> =
        listOf(
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
            DataRequestEntity(
                userId = "1234",
                dataType = "p2p",
                reportingPeriod = "dummyPeriod",
                creationTimestamp = 0,
                datalandCompanyId = "dummyCompanyId",
            ),
        )

    private val dummyRequestChangeReason = "dummy reason"

    private val dummyNonSourceableInfo =
        NonSourceableInfo(
            companyId = "",
            dataType = DataTypeEnum.p2p,
            reportingPeriod = "",
            isNonSourceable = true,
            reason = dummyRequestChangeReason,
        )

    private val dummySourceableInfo =
        NonSourceableInfo(
            companyId = "",
            dataType = DataTypeEnum.p2p,
            reportingPeriod = "",
            isNonSourceable = false,
            reason = dummyRequestChangeReason,
        )

    private val dummyDataRequestEntity: DataRequestEntity = dummyDataRequestEntities[0]

    private val metaData =
        DataMetaInformation(
            dataId = UUID.randomUUID().toString(),
            companyId = "companyId",
            dataType = DataTypeEnum.p2p,
            uploadTime = 0,
            reportingPeriod = "",
            currentlyActive = false,
            qaStatus = QaStatus.Accepted,
            ref = "test",
        )
    private val dummyMessage =
        StoredDataRequestMessageObject(
            contacts = setOf("test@example.com"),
            message = "test message",
            creationTimestamp = Instant.now().toEpochMilli(),
        )

    private val dummyAdminComment = "test comment"

    private fun mockRepos() {
        mockDataRequestRepository = mock(DataRequestRepository::class.java)
        `when`<Any>(
            mockDataRequestRepository.findById(dataRequestId),
        ).thenReturn(Optional.of(dummyDataRequestEntity))
        dummyDataRequestEntities.forEach {
            `when`<Any>(
                mockDataRequestRepository.findById(it.dataRequestId),
            ).thenReturn(Optional.of(it))
        }
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        dataType = setOf(metaData.dataType),
                        datalandCompanyId = metaData.companyId,
                        reportingPeriod = metaData.reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
            ),
        ).thenReturn(dummyDataRequestEntities)

        `when`(
            mockDataRequestRepository.findAllByDatalandCompanyIdAndDataTypeAndReportingPeriod(
                datalandCompanyId = dummyNonSourceableInfo.companyId,
                dataType = dummyNonSourceableInfo.dataType.toString(),
                reportingPeriod = dummyNonSourceableInfo.reportingPeriod,
            ),
        ).thenReturn(listOf(dummyDataRequestEntity))

        mockDataRequestProcessingUtils = mock(DataRequestProcessingUtils::class.java)
        doNothing().`when`(mockDataRequestProcessingUtils).addNewRequestStatusToHistory(
            any(), any(),
            any(), anyString(),
            any(),
        )
        doNothing().`when`(mockDataRequestProcessingUtils).addMessageToMessageHistory(
            any(), anySet(), anyString(), any(),
        )
    }

    @BeforeEach
    fun setupDataRequestAlterationManager() {
        mockRepos()

        mockRequestEmailManager = mock(RequestEmailManager::class.java)
        mockCompanyRolesManager = mock(CompanyRolesManager::class.java)

        mockMetaControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(mockMetaControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)

        dataRequestAlterationManager =
            DataRequestAlterationManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestLogger = mock(DataRequestLogger::class.java),
                metaDataControllerApi = mockMetaControllerApi,
                requestEmailManager = mockRequestEmailManager,
                companyRolesManager = mockCompanyRolesManager,
                utils = mockDataRequestProcessingUtils,
            )
    }

    @BeforeEach
    fun setupSecurityMock() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "user@example.com",
                "1234-221-1111elf",
                setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that a request response email is sent when a request status is patched to answered or closed`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Answered,
            null,
        )
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenStatusChanged(
                any(), eq(RequestStatus.Answered), eq(null), eq(null),
            )
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Closed,
            null,
        )
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenStatusChanged(
                any(), eq(RequestStatus.Closed), eq(null), eq(null),
            )
        verify(mockDataRequestProcessingUtils, times(2))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
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

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(),
            )

        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that a request answered email is sent when request statuses are patched from open to answered`() {
        dataRequestAlterationManager.patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(metaData.dataId, correlationId)
        dummyDataRequestEntities.forEach {
            verify(mockRequestEmailManager)
                .sendEmailsWhenStatusChanged(eq(it), eq(RequestStatus.Answered), eq(null), anyString())
        }
        verify(mockDataRequestProcessingUtils, times(dummyDataRequestEntities.size))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
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

        verify(mockRequestEmailManager, times(1))
            .sendSingleDataRequestEmail(
                any(), anySet(), anyString(),
            )

        verify(mockDataRequestProcessingUtils, times(1))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )

        verify(mockDataRequestProcessingUtils, times(0))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), anyString(),
                any(),
            )
    }

    @Test
    fun `validate that no email is sent when both request priority and admin comment are patched`() {
        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestStatus = null,
            accessStatus = null,
            message = null,
            contacts = null,
            requestPriority = RequestPriority.Low,
            adminComment = "test",
        )

        verify(mockRequestEmailManager, times(0))
            .sendSingleDataRequestEmail(
                any(), anySet(), anyString(),
            )
    }

    @Test
    fun `validate that the modification time remains unchanged when only the admin comment is patched`() {
        val originalModificationTime = dummyDataRequestEntity.lastModifiedDate

        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            adminComment = dummyAdminComment,
        )

        assertEquals(originalModificationTime, dummyDataRequestEntity.lastModifiedDate)
        assertEquals(dummyAdminComment, dummyDataRequestEntity.adminComment)
    }

    @Test
    fun `validate that the modification time changes if the request priority is patched`() {
        val originalModificationTime = dummyDataRequestEntity.lastModifiedDate

        dataRequestAlterationManager.patchDataRequest(
            dataRequestId = dataRequestId,
            requestPriority = RequestPriority.High,
        )

        assertFalse(originalModificationTime == dummyDataRequestEntity.lastModifiedDate)
        assertEquals(RequestPriority.High, dummyDataRequestEntity.requestPriority)
    }

    @Test
    fun `validate that patching corresponding requests for a dataset only changed the corresponding requests`() {
        nonSourceableDataManager =
            NonSourceableDataManager(
                dataRequestAlterationManager = dataRequestAlterationManager,
                dataRequestRepository = mockDataRequestRepository,
            )
        nonSourceableDataManager.patchAllRequestsForThisDatasetToStatusNonSourceable(dummyNonSourceableInfo, correlationId)

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), anyString(),
                any(),
            )
    }

    @Test
    fun `validate that providing information about a dataset that is sourceable throws an IllegalArgumentException`() {
        nonSourceableDataManager =
            NonSourceableDataManager(
                dataRequestAlterationManager = dataRequestAlterationManager,
                dataRequestRepository = mockDataRequestRepository,
            )

        assertThrows<IllegalArgumentException> {
            nonSourceableDataManager.patchAllRequestsForThisDatasetToStatusNonSourceable(dummySourceableInfo, correlationId)
        }
    }
}
