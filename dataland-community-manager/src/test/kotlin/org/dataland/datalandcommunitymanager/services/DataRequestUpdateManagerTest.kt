@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.dataland.datalandcommunitymanager.services

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.QaReviewResponse
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestUpdateManagerTest {
    private lateinit var dataRequestUpdateManager: DataRequestUpdateManager
    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockDataRequestLogger = mock<DataRequestLogger>()
    private val mockDataRequestRepository = mock<DataRequestRepository>()
    private val mockDataRequestSummaryNotificationService = mock<DataRequestSummaryNotificationService>()
    private val mockMetaDataControllerApi = mock<MetaDataControllerApi>()
    private val mockQaControllerApi = mock<QaControllerApi>()
    private val mockRequestStatusRepository = mock<RequestStatusRepository>()
    private val mockMessageRepository = mock<MessageRepository>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockExceptionForwarder = mock<ExceptionForwarder>()

    private lateinit var dataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var spyDataRequestProcessingUtils: DataRequestProcessingUtils
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockRequestEmailManager = mock<RequestEmailManager>()
    private lateinit var dataRequestUpdateUtils: DataRequestUpdateUtils
    private lateinit var dummyDataRequestEntitiesWithoutEarlierQaApproval: List<DataRequestEntity>
    private lateinit var dummyChildCompanyDataRequestEntities: List<DataRequestEntity>
    private lateinit var mockQaReviewResponsesWithoutEarlierApproval: List<QaReviewResponse>
    private lateinit var mockQaReviewResponsesWithEarlierApproval: List<QaReviewResponse>

    private val correlationId = UUID.randomUUID().toString()

    private val dummyRequestChangeReason = "dummy reason"
    private val dummyCompanyId = "dummyCompanyId"

    private val dummyNonSourceableInfo =
        SourceabilityInfo(
            companyId = dummyCompanyId,
            dataType = DataTypeEnum.p2p,
            reportingPeriod = "dummyPeriod",
            isNonSourceable = true,
            reason = dummyRequestChangeReason,
        )

    private val dummySourceableInfo =
        SourceabilityInfo(
            companyId = "",
            dataType = DataTypeEnum.p2p,
            reportingPeriod = "",
            isNonSourceable = false,
            reason = dummyRequestChangeReason,
        )

    private lateinit var dummyDataRequestEntityWithoutEarlierQaApproval1: DataRequestEntity
    private lateinit var dummyDataRequestEntityWithoutEarlierQaApproval2: DataRequestEntity
    private lateinit var dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval: DataRequestEntity
    private lateinit var dummyChildCompanyDataRequestEntityWithEarlierQaApproval: DataRequestEntity
    private lateinit var dummyDataRequestEntityWithdrawn: DataRequestEntity

    private val metaData =
        DataMetaInformation(
            dataId = UUID.randomUUID().toString(),
            companyId = dummyCompanyId,
            dataType = DataTypeEnum.p2p,
            uploadTime = 0,
            reportingPeriod = "dummyPeriod",
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
        dummyDataRequestEntitiesWithoutEarlierQaApproval.forEach {
            doReturn(it)
                .whenever(mockDataRequestRepository)
                .findByRequestId(it.dataRequestId)
        }
        doReturn(dummyDataRequestEntityWithdrawn)
            .whenever(mockDataRequestRepository)
            .findByRequestId(dummyDataRequestEntityWithdrawn.dataRequestId)
        doReturn(dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .findByRequestId(dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval.dataRequestId)
        doReturn(dummyChildCompanyDataRequestEntityWithEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .findByRequestId(dummyChildCompanyDataRequestEntityWithEarlierQaApproval.dataRequestId)
        doReturn(dummyDataRequestEntitiesWithoutEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        dataType = setOf(metaData.dataType),
                        datalandCompanyIds = setOf(metaData.companyId),
                        reportingPeriod = metaData.reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
            )
        doReturn(dummyChildCompanyDataRequestEntities)
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        setOf(metaData.dataType), null, null,
                        setOf("dummyChildCompanyId1", "dummyChildCompanyId2"),
                        metaData.reportingPeriod,
                        setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                        null, null, null,
                    ),
            )
        doReturn(dummyDataRequestEntitiesWithoutEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .findAllByDatalandCompanyIdAndDataTypeAndReportingPeriod(
                datalandCompanyId = dummyNonSourceableInfo.companyId,
                dataType = dummyNonSourceableInfo.dataType.toString(),
                reportingPeriod = dummyNonSourceableInfo.reportingPeriod,
            )
    }

    private fun mockCompanyRelatedInformation() {
        doReturn("dummyCompany").whenever(mockCompanyInfoService).getValidCompanyName(dummyCompanyId)
        doReturn("dummyChildCompany1").whenever(mockCompanyInfoService).getValidCompanyName("dummyChildCompanyId1")
        doReturn("dummyChildCompany2").whenever(mockCompanyInfoService).getValidCompanyName("dummyChildCompanyId2")
        doReturn(listOf<BasicCompanyInformation>())
            .whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(any())
        doReturn(
            listOf(
                BasicCompanyInformation(
                    companyName = "dummyChildCompany1",
                    companyId = "dummyChildCompanyId1",
                    headquarters = "",
                    countryCode = "",
                ),
                BasicCompanyInformation(
                    companyName = "dummyChildCompany2",
                    companyId = "dummyChildCompanyId2",
                    headquarters = "",
                    countryCode = "",
                ),
            ),
        ).whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(metaData.companyId)
    }

    private fun mockMetaDataAndQaReviewResponses() {
        doReturn(metaData).whenever(mockMetaDataControllerApi).getDataMetaInfo(metaData.dataId)
        mockQaReviewResponsesWithoutEarlierApproval =
            listOf(mock<QaReviewResponse>())
        mockQaReviewResponsesWithEarlierApproval =
            listOf(mock<QaReviewResponse>(), mock<QaReviewResponse>())
        doReturn(mockQaReviewResponsesWithoutEarlierApproval)
            .whenever(mockQaControllerApi)
            .getInfoOnDatasets(any(), eq(setOf("dummyPeriod")), any(), any(), any(), any())
        doReturn(mockQaReviewResponsesWithoutEarlierApproval)
            .whenever(mockQaControllerApi)
            .getInfoOnDatasets(any(), eq(setOf("dummyPeriod")), eq("dummyChildCompany1"), any(), any(), any())
        doReturn(mockQaReviewResponsesWithEarlierApproval)
            .whenever(mockQaControllerApi)
            .getInfoOnDatasets(any(), eq(setOf("dummyPeriod")), eq("dummyChildCompany2"), any(), any(), any())
    }

    private fun setupDataRequestUpdateManager() {
        dataRequestProcessingUtils =
            DataRequestProcessingUtils(
                mockDataRequestRepository,
                mockRequestStatusRepository,
                mockMessageRepository,
                mockDataRequestLogger,
                mockCompanyDataControllerApi,
                mockMetaDataControllerApi,
                mockExceptionForwarder,
            )
        spyDataRequestProcessingUtils = spy(dataRequestProcessingUtils)
        dataRequestUpdateUtils =
            DataRequestUpdateUtils(
                processingUtils = spyDataRequestProcessingUtils,
                dataRequestLogger = mockDataRequestLogger,
                companyInfoService = mockCompanyInfoService,
                companyRolesManager = mockCompanyRolesManager,
                requestEmailManager = mockRequestEmailManager,
                qaControllerApi = mockQaControllerApi,
            )
        dataRequestUpdateManager =
            DataRequestUpdateManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestSummaryNotificationService = mockDataRequestSummaryNotificationService,
                dataRequestLogger = mockDataRequestLogger,
                requestEmailManager = mockRequestEmailManager,
                metaDataControllerApi = mockMetaDataControllerApi,
                dataRequestUpdateUtils = dataRequestUpdateUtils,
                companyDataControllerApi = mockCompanyDataControllerApi,
            )
    }

    private fun awaitUntilAsserted(operation: () -> Any) =
        Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).untilAsserted {
            operation()
        }

    @BeforeEach
    fun setupDummyDataRequestEntities() {
        TestUtils.mockSecurityContext("user@example.com", "1234-221-1111elf", DatalandRealmRole.ROLE_USER)
        dummyDataRequestEntitiesWithoutEarlierQaApproval =
            listOf(
                DataRequestEntity(
                    userId = "4321", dataType = "p2p", notifyMeImmediately = true,
                    reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                    datalandCompanyId = dummyCompanyId,
                ),
                DataRequestEntity(
                    userId = "1234", dataType = "p2p", notifyMeImmediately = false,
                    reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                    datalandCompanyId = dummyCompanyId,
                ),
            )
        dummyDataRequestEntityWithoutEarlierQaApproval1 = dummyDataRequestEntitiesWithoutEarlierQaApproval[0]
        dummyDataRequestEntityWithoutEarlierQaApproval2 = dummyDataRequestEntitiesWithoutEarlierQaApproval[1]
        dummyDataRequestEntityWithdrawn =
            dummyDataRequestEntityWithoutEarlierQaApproval1.copy(
                dataRequestId = UUID.randomUUID().toString(),
                creationTimestamp = 0L,
            )
        dummyDataRequestEntityWithdrawn.addToDataRequestStatusHistory(
            RequestStatusEntity(
                StoredDataRequestStatusObject(
                    status = RequestStatus.Withdrawn,
                    creationTimestamp = 1L,
                    accessStatus = AccessStatus.Public,
                    requestStatusChangeReason = null,
                    answeringDataId = null,
                ),
                dummyDataRequestEntityWithdrawn,
            ),
        )
        dummyChildCompanyDataRequestEntities =
            listOf(
                DataRequestEntity(
                    userId = "1234", dataType = "p2p", notifyMeImmediately = true,
                    reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                    datalandCompanyId = "dummyChildCompanyId1",
                ),
                DataRequestEntity(
                    userId = "1234", dataType = "p2p", notifyMeImmediately = false,
                    reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                    datalandCompanyId = "dummyChildCompanyId2",
                ),
            )
        dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval = dummyChildCompanyDataRequestEntities[0]
        dummyChildCompanyDataRequestEntityWithEarlierQaApproval = dummyChildCompanyDataRequestEntities[1]
    }

    @BeforeEach
    fun setupMocksAndDummyRequests() {
        reset(
            mockCompanyInfoService,
            mockDataRequestLogger,
            mockDataRequestRepository,
            mockDataRequestSummaryNotificationService,
            mockMetaDataControllerApi,
            mockQaControllerApi,
            mockCompanyRolesManager,
            mockRequestEmailManager,
            mockCompanyDataControllerApi,
            mockExceptionForwarder,
            mockMessageRepository,
            mockRequestStatusRepository,
        )
        mockRepos()
        mockCompanyRelatedInformation()
        mockMetaDataAndQaReviewResponses()
        setupDataRequestUpdateManager()
    }

    @Test
    fun `validate that notification behaviour is as expected when a request status is patched to answered and flag is active`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Answered),
            correlationId = correlationId,
        )
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Answered), eq(false), eq(correlationId),
            )
        verify(spyDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )
        verify(spyDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )
        verify(mockDataRequestSummaryNotificationService, times(1))
            .createUserSpecificNotificationEvent(
                eq(dummyDataRequestEntityWithoutEarlierQaApproval1), eq(RequestStatus.Answered),
                eq(true), eq(false),
            )
    }

    @Test
    fun `validate that no request response email is sent when a request status is patched to answered and flag is inactive`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval2.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Answered),
            correlationId,
        )
        verify(mockRequestEmailManager, times(0))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Answered), any(), eq(correlationId),
            )
        verify(spyDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )
        verify(spyDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )
    }

    @Test
    fun `validate that no request response email is sent when a request status is patched to closed and flag is active`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Closed),
            correlationId,
        )
        verify(mockRequestEmailManager, times(0))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Closed), any(), eq(correlationId),
            )
        verify(spyDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )
        verify(spyDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )
    }

    @Test
    fun `validate that no email is sent to company contacts and the history is updated when an access status is patched`() {
        val randomUUID = UUID.randomUUID().toString()
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = DataRequestPatch(accessStatus = AccessStatus.Pending),
            randomUUID,
        )

        verify(spyDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), anyOrNull(),
            )

        verify(spyDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )
    }

    @Test
    fun `validate that answer emails for subsidiaries are sent according to flag on request status patch from open to answered`() {
        dataRequestUpdateManager.patchRequestStatusToAnsweredForParentAndSubsidiaries(
            dummyDataRequestEntitiesWithoutEarlierQaApproval,
            metaData.dataId,
            correlationId,
        )
        val expectedNumberOfEmailsPerRequest = listOf(1, 0, 1)
        for (i in 0..1) {
            verify(mockRequestEmailManager, times(expectedNumberOfEmailsPerRequest[i]))
                .sendEmailsWhenRequestStatusChanged(
                    eq(dummyDataRequestEntitiesWithoutEarlierQaApproval[i]),
                    eq(RequestStatus.Answered),
                    eq(false),
                    eq(correlationId),
                )
        }
        val expectedNumberOfEmailsPerChildRequest = listOf(1, 0)
        val earlierQaApprovalExistenceInformation = listOf(false, true)
        for (i in 0..1) {
            verify(mockRequestEmailManager, times(expectedNumberOfEmailsPerChildRequest[i]))
                .sendEmailsWhenRequestStatusChanged(
                    eq(dummyChildCompanyDataRequestEntities[i]),
                    eq(RequestStatus.Answered),
                    eq(earlierQaApprovalExistenceInformation[i]),
                    any<String>(),
                )
        }
        verify(spyDataRequestProcessingUtils, times(dummyDataRequestEntitiesWithoutEarlierQaApproval.size))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), eq(null),
                any(), any(),
            )
        dummyChildCompanyDataRequestEntities.forEach {
            verify(spyDataRequestProcessingUtils, times(1))
                .addNewRequestStatusToHistory(
                    eq(it), any(),
                    any(), any<String>(),
                    any(), any(),
                )
        }
        verify(spyDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )
    }

    @Test
    fun `validate that the sending of a request email is triggered when a request message is added`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch =
                DataRequestPatch(
                    contacts = dummyMessage.contacts,
                    message = dummyMessage.message,
                ),
            correlationId = correlationId,
        )

        verify(mockRequestEmailManager, times(1))
            .sendSingleDataRequestEmail(
                any(), any<Set<String>>(), any<String>(),
            )

        verify(spyDataRequestProcessingUtils, times(1))
            .addMessageToMessageHistory(
                any(), any<Set<String>>(), any<String>(), any(),
            )

        verify(spyDataRequestProcessingUtils, times(0))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any<String>(),
                any(), any(),
            )
    }

    @Test
    fun `validate that no email is sent when both request priority and admin comment are patched`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch =
                DataRequestPatch(
                    requestPriority = RequestPriority.Low,
                    adminComment = "test",
                ),
            correlationId = correlationId,
        )

        verify(mockRequestEmailManager, times(0))
            .sendSingleDataRequestEmail(
                any(), any<Set<String>>(), any<String>(),
            )
    }

    @Test
    fun `validate that the modification time remains unchanged when only the admin comment is patched`() {
        val originalModificationTime = dummyDataRequestEntityWithoutEarlierQaApproval1.lastModifiedDate

        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = DataRequestPatch(adminComment = dummyAdminComment),
            correlationId = correlationId,
        )

        assertEquals(originalModificationTime, dummyDataRequestEntityWithoutEarlierQaApproval1.lastModifiedDate)
        assertEquals(dummyAdminComment, dummyDataRequestEntityWithoutEarlierQaApproval1.adminComment)
    }

    @Test
    fun `validate that the modification time changes if the request priority is patched`() {
        val originalModificationTime = dummyDataRequestEntityWithoutEarlierQaApproval1.lastModifiedDate

        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = DataRequestPatch(requestPriority = RequestPriority.High),
            correlationId = correlationId,
        )

        awaitUntilAsserted {
            assertFalse(originalModificationTime == dummyDataRequestEntityWithoutEarlierQaApproval1.lastModifiedDate)
            assertEquals(RequestPriority.High, dummyDataRequestEntityWithoutEarlierQaApproval1.requestPriority)
        }
    }

    @Test
    fun `validate that patching corresponding requests for a dataset only changes the corresponding requests`() {
        dataRequestUpdateManager.patchAllRequestsToStatusNonSourceable(
            dummyNonSourceableInfo,
            correlationId,
        )

        verify(spyDataRequestProcessingUtils, times(2))
            .addNewRequestStatusToHistory(
                any(), any(),
                any(), any<String>(),
                any(), anyOrNull(),
            )
    }

    @Test
    fun `validate that providing information about a dataset that is sourceable throws an IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            dataRequestUpdateManager.patchAllRequestsToStatusNonSourceable(
                dummySourceableInfo,
                correlationId,
            )
        }
    }

    @Test
    fun `validate that notification behaviour is as expected when requests are patched from Open to NonSourceable`() {
        dataRequestUpdateManager.patchAllRequestsToStatusNonSourceable(dummyNonSourceableInfo, correlationId)
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenRequestStatusChanged(
                eq(dummyDataRequestEntityWithoutEarlierQaApproval1),
                eq(RequestStatus.NonSourceable),
                eq(false),
                eq(correlationId),
            )
        verify(mockDataRequestSummaryNotificationService, times(1))
            .createUserSpecificNotificationEvent(
                eq(dummyDataRequestEntityWithoutEarlierQaApproval1),
                eq(RequestStatus.NonSourceable),
                eq(true),
                eq(false),
            )
        verify(mockDataRequestSummaryNotificationService, times(1))
            .createUserSpecificNotificationEvent(
                eq(dummyDataRequestEntityWithoutEarlierQaApproval2),
                eq(RequestStatus.NonSourceable),
                eq(false),
                eq(false),
            )
    }

    @Test
    fun `validate that requests can be patched from status Withdrawn to Open`() {
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dummyDataRequestEntityWithdrawn.dataRequestId,
            DataRequestPatch(requestStatus = RequestStatus.Open),
            correlationId,
        )
        assertEquals(RequestStatus.Open, dummyDataRequestEntityWithoutEarlierQaApproval1.requestStatus)
    }
}
