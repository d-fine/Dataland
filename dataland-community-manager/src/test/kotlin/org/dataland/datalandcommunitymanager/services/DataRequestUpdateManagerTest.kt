package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.Optional
import java.util.UUID

class DataRequestUpdateManagerTest {
    private lateinit var dataRequestUpdateManager: DataRequestUpdateManager
    private lateinit var mockAuthentication: DatalandJwtAuthentication
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var mockNotificationService: NotificationService
    private lateinit var mockMetaControllerApi: MetaDataControllerApi
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockRequestEmailManager: RequestEmailManager
    private lateinit var mockCompanyRolesManager: CompanyRolesManager
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()

    private val correlationId = UUID.randomUUID().toString()
    private lateinit var dummyDataRequestEntities: List<DataRequestEntity>

    private val dummyRequestChangeReason = "dummy reason"
    private val dummyCompanyId = "dummyCompanyId"

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

    private lateinit var dummyDataRequestEntity: DataRequestEntity
    private lateinit var dummyChildCompanyDataRequestEntity: DataRequestEntity

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
            mockDataRequestRepository.findById(dummyDataRequestEntity.dataRequestId),
        ).thenReturn(Optional.of(dummyDataRequestEntity))
        mockNotificationService = mock(NotificationService::class.java)
        dummyDataRequestEntities.forEach {
            `when`<Any>(
                mockDataRequestRepository.findById(it.dataRequestId),
            ).thenReturn(Optional.of(it))
        }
        doReturn(Optional.of(dummyChildCompanyDataRequestEntity))
            .whenever(mockDataRequestRepository)
            .findById(dummyChildCompanyDataRequestEntity.dataRequestId)
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        dataType = setOf(metaData.dataType),
                        datalandCompanyIds = setOf(metaData.companyId),
                        reportingPeriod = metaData.reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
            ),
        ).thenReturn(dummyDataRequestEntities)
        doReturn(listOf(dummyChildCompanyDataRequestEntity))
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        setOf(metaData.dataType), null, null, setOf("dummyChildCompanyId"), metaData.reportingPeriod,
                        setOf(RequestStatus.Open, RequestStatus.NonSourceable), null, null, null,
                    ),
            )

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
            any(), any(),
        )
        doNothing().`when`(mockDataRequestProcessingUtils).addMessageToMessageHistory(
            any(), anySet(), anyString(), any(),
        )
    }

    private fun setupDataRequestAlterationManager() {
        mockRequestEmailManager = mock(RequestEmailManager::class.java)
        mockCompanyRolesManager = mock(CompanyRolesManager::class.java)

        mockMetaControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(mockMetaControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)

        doReturn(listOf<BasicCompanyInformation>())
            .whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(any())
        doReturn(listOf(BasicCompanyInformation(companyName = "", companyId = "dummyChildCompanyId", headquarters = "", countryCode = "")))
            .whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(metaData.companyId)

        dataRequestUpdateManager =
            DataRequestUpdateManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestLogger = mock(DataRequestLogger::class.java),
                metaDataControllerApi = mockMetaControllerApi,
                requestEmailManager = mockRequestEmailManager,
                companyRolesManager = mockCompanyRolesManager,
                utils = mockDataRequestProcessingUtils,
                companyDataControllerApi = mockCompanyDataControllerApi,
                notificationService = mockNotificationService,
            )
    }

    private fun setupSecurityMock() {
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

    private fun setupDummyDataRequestEntities() {
        dummyDataRequestEntities =
            listOf(
                DataRequestEntity(
                    userId = "",
                    dataType = "p2p",
                    emailOnUpdate = true,
                    reportingPeriod = "",
                    creationTimestamp = 0,
                    datalandCompanyId = "",
                ),
                DataRequestEntity(
                    userId = "dummyId",
                    dataType = "dummyDataType",
                    emailOnUpdate = true,
                    reportingPeriod = "dummyPeriod",
                    creationTimestamp = 123456,
                    datalandCompanyId = dummyCompanyId,
                ),
                DataRequestEntity(
                    userId = "1234",
                    dataType = "p2p",
                    emailOnUpdate = true,
                    reportingPeriod = "dummyPeriod",
                    creationTimestamp = 0,
                    datalandCompanyId = dummyCompanyId,
                ),
            )
        dummyDataRequestEntity = dummyDataRequestEntities[0]
        dummyChildCompanyDataRequestEntity =
            DataRequestEntity(
                userId = "1234",
                dataType = "p2p",
                true,
                reportingPeriod = "dummyPeriod",
                creationTimestamp = 0,
                datalandCompanyId = "dummyChildCompanyId",
            )
    }

    @BeforeEach
    fun setupMocksAndDummyRequests() {
        setupSecurityMock()
        setupDummyDataRequestEntities()
        mockRepos()
        setupDataRequestAlterationManager()
    }

    @Test
    fun `validate that a request response email is sent when a request status is patched to answered and flag is active`() {
        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Answered),
            correlationId,
        )
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Answered), eq(correlationId),
            )
        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that no request response email is sent when a request status is patched to closed and flag is active`() {
        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Closed),
            correlationId,
        )
        verify(mockRequestEmailManager, times(0))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Closed), eq(correlationId),
            )
        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that no email is sent and the history is updated when an access status is patched`() {
        val randomUUID = UUID.randomUUID().toString()
        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch = DataRequestPatch(accessStatus = AccessStatus.Pending),
            randomUUID,
        )

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )

        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that a request answered email is sent when request statuses are patched from open to answered`() {
        dataRequestUpdateManager.patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(metaData.dataId, correlationId)
        dummyDataRequestEntities.forEach {
            verify(mockRequestEmailManager)
                .sendEmailsWhenRequestStatusChanged(eq(it), eq(RequestStatus.Answered), eq(correlationId))
        }
        verify(mockRequestEmailManager)
            .sendEmailsWhenRequestStatusChanged(
                eq(dummyChildCompanyDataRequestEntity),
                eq(RequestStatus.Answered),
                anyString(),
            )
        verify(mockDataRequestProcessingUtils, times(dummyDataRequestEntities.size))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), any(),
            )
        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                eq(dummyChildCompanyDataRequestEntity), any(),
                any(), anyString(),
                any(), any(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), anySet(), anyString(), any(),
            )
    }

    @Test
    fun `validate that the sending of a request email is triggered when a request message is added`() {
        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch =
                DataRequestPatch(
                    contacts = dummyMessage.contacts,
                    message = dummyMessage.message,
                ),
            correlationId = correlationId,
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
                any(), any(),
            )
    }

    @Test
    fun `validate that no email is sent when both request priority and admin comment are patched`() {
        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch =
                DataRequestPatch(
                    requestPriority = RequestPriority.Low,
                    adminComment = "test",
                ),
            correlationId = correlationId,
        )

        verify(mockRequestEmailManager, times(0))
            .sendSingleDataRequestEmail(
                any(), anySet(), anyString(),
            )
    }

    @Test
    fun `validate that the modification time remains unchanged when only the admin comment is patched`() {
        val originalModificationTime = dummyDataRequestEntity.lastModifiedDate

        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch = DataRequestPatch(adminComment = dummyAdminComment),
            correlationId = correlationId,
        )

        assertEquals(originalModificationTime, dummyDataRequestEntity.lastModifiedDate)
        assertEquals(dummyAdminComment, dummyDataRequestEntity.adminComment)
    }

    @Test
    fun `validate that the modification time changes if the request priority is patched`() {
        val originalModificationTime = dummyDataRequestEntity.lastModifiedDate

        dataRequestUpdateManager.patchDataRequest(
            dataRequestId = dummyDataRequestEntity.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestPriority = RequestPriority.High),
            correlationId = correlationId,
        )

        assertFalse(originalModificationTime == dummyDataRequestEntity.lastModifiedDate)
        assertEquals(RequestPriority.High, dummyDataRequestEntity.requestPriority)
    }

    @Test
    fun `validate that patching corresponding requests for a dataset only changed the corresponding requests`() {
        dataRequestUpdateManager.patchAllRequestsForThisDatasetToStatusNonSourceable(dummyNonSourceableInfo, correlationId)

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), anyString(),
                any(), anyOrNull(),
            )
    }

    @Test
    fun `validate that providing information about a dataset that is sourceable throws an IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            dataRequestUpdateManager.patchAllRequestsForThisDatasetToStatusNonSourceable(dummySourceableInfo, correlationId)
        }
    }
}
