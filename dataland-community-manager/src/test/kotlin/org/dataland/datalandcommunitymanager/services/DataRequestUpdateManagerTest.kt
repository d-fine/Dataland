@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateManagerTestDataProvider
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.QaReviewResponse
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestUpdateManagerTest {
    companion object {
        private val testDataProvider = DataRequestUpdateManagerTestDataProvider()

        @JvmStatic
        fun provideParametersToCheckDataRequestUpdateUtils() = testDataProvider.getStreamOfArgumentsToTestDataRequestUpdateUtils()
    }

    private lateinit var dataRequestUpdateManager: DataRequestUpdateManager
    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockDataRequestLogger = mock<DataRequestLogger>()
    private val mockRequestEmailManager = mock<RequestEmailManager>()
    private val mockDataRequestRepository = mock<DataRequestRepository>()
    private val mockDataRequestSummaryNotificationService = mock<DataRequestSummaryNotificationService>()
    private val mockMetaDataControllerApi = mock<MetaDataControllerApi>()
    private val mockQaControllerApi = mock<QaControllerApi>()
    private val mockRequestStatusRepository = mock<RequestStatusRepository>()
    private val mockMessageRepository = mock<MessageRepository>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockExceptionForwarder = mock<ExceptionForwarder>()
    private val mockDataRequestUpdateUtils = mock<DataRequestUpdateUtils>()
    private lateinit var dummyDataRequestEntitiesWithoutEarlierQaApproval: List<DataRequestEntity>
    private lateinit var dummyChildCompanyDataRequestEntities: List<DataRequestEntity>
    private lateinit var mockQaReviewResponsesWithoutEarlierApproval: List<QaReviewResponse>
    private lateinit var mockQaReviewResponsesWithEarlierApproval: List<QaReviewResponse>

    private val correlationId = UUID.randomUUID().toString()
    private val dummyCompanyId = "dummyCompanyId"
    private val dummyNonSourceableInfo = testDataProvider.getDummyNonSourceableInfo()
    private val dummySourceableInfo = testDataProvider.getDummySourceableInfo()
    private val dataMetaInformation = testDataProvider.getDataMetaInformation()

    private lateinit var dummyDataRequestEntityWithoutEarlierQaApproval1: DataRequestEntity
    private lateinit var dummyDataRequestEntityWithoutEarlierQaApproval2: DataRequestEntity
    private lateinit var dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval: DataRequestEntity
    private lateinit var dummyChildCompanyDataRequestEntityWithEarlierQaApproval: DataRequestEntity
    private lateinit var dummyDataRequestEntityWithdrawn: DataRequestEntity

    private fun mockRepos() {
        dummyDataRequestEntitiesWithoutEarlierQaApproval.forEach {
            doReturn(it)
                .whenever(mockDataRequestRepository)
                .findByDataRequestId(it.dataRequestId)
        }
        doReturn(dummyDataRequestEntityWithdrawn)
            .whenever(mockDataRequestRepository)
            .findByDataRequestId(dummyDataRequestEntityWithdrawn.dataRequestId)
        doReturn(dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .findByDataRequestId(dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval.dataRequestId)
        doReturn(dummyChildCompanyDataRequestEntityWithEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .findByDataRequestId(dummyChildCompanyDataRequestEntityWithEarlierQaApproval.dataRequestId)
        doReturn(dummyDataRequestEntitiesWithoutEarlierQaApproval)
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        dataType = setOf(dataMetaInformation.dataType),
                        datalandCompanyIds = setOf(dataMetaInformation.companyId),
                        reportingPeriod = dataMetaInformation.reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
            )
        doReturn(dummyChildCompanyDataRequestEntities)
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                searchFilter =
                    DataRequestsFilter(
                        setOf(dataMetaInformation.dataType), null, null,
                        setOf("dummyChildCompanyId1", "dummyChildCompanyId2"),
                        dataMetaInformation.reportingPeriod,
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

    private fun mockDataRequestUpdateUtils() {
        dummyDataRequestEntitiesWithoutEarlierQaApproval.forEach {
            doReturn(false)
                .whenever(mockDataRequestUpdateUtils)
                .existsEarlierQaApprovalOfDatasetForDataDimension(it)
        }
        doReturn(false)
            .whenever(mockDataRequestUpdateUtils)
            .existsEarlierQaApprovalOfDatasetForDataDimension(
                dummyChildCompanyDataRequestEntityWithoutEarlierQaApproval,
            )
        doReturn(true)
            .whenever(mockDataRequestUpdateUtils)
            .existsEarlierQaApprovalOfDatasetForDataDimension(
                dummyChildCompanyDataRequestEntityWithEarlierQaApproval,
            )
    }

    private fun mockCompanyRelatedInformation() {
        doReturn("dummyCompany").whenever(mockCompanyInfoService).getValidCompanyName(dummyCompanyId)
        doReturn("dummyChildCompany1").whenever(mockCompanyInfoService).getValidCompanyName("dummyChildCompanyId1")
        doReturn("dummyChildCompany2").whenever(mockCompanyInfoService).getValidCompanyName("dummyChildCompanyId2")
        doReturn(listOf<BasicCompanyInformation>())
            .whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(any())
        doReturn(testDataProvider.getListOfBasicCompanyInformationForSubsidiaries())
            .whenever(mockCompanyDataControllerApi)
            .getCompanySubsidiariesByParentId(dataMetaInformation.companyId)
    }

    private fun mockMetaDataAndQaReviewResponses() {
        doReturn(dataMetaInformation).whenever(mockMetaDataControllerApi).getDataMetaInfo(dataMetaInformation.dataId)
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
        dataRequestUpdateManager =
            DataRequestUpdateManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestSummaryNotificationService = mockDataRequestSummaryNotificationService,
                dataRequestLogger = mockDataRequestLogger,
                requestEmailManager = mockRequestEmailManager,
                metaDataControllerApi = mockMetaDataControllerApi,
                dataRequestUpdateUtils = mockDataRequestUpdateUtils,
                companyDataControllerApi = mockCompanyDataControllerApi,
            )
    }

    @BeforeEach
    fun setupDummyDataRequestEntities() {
        TestUtils.mockSecurityContext("user@example.com", "1234-221-1111elf", DatalandRealmRole.ROLE_USER)
        dummyDataRequestEntitiesWithoutEarlierQaApproval = testDataProvider.getDummyDataRequestEntities()
        dummyDataRequestEntityWithoutEarlierQaApproval1 = dummyDataRequestEntitiesWithoutEarlierQaApproval[0]
        dummyDataRequestEntityWithoutEarlierQaApproval2 = dummyDataRequestEntitiesWithoutEarlierQaApproval[1]
        dummyDataRequestEntityWithdrawn = testDataProvider.getDummyDataRequestEntityWithdrawn()
        dummyChildCompanyDataRequestEntities = testDataProvider.getDummyChildCompanyDataRequestEntities()
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
            mockRequestEmailManager,
            mockCompanyDataControllerApi,
            mockExceptionForwarder,
            mockMessageRepository,
            mockRequestStatusRepository,
        )
        mockRepos()
        mockDataRequestUpdateUtils()
        mockCompanyRelatedInformation()
        mockMetaDataAndQaReviewResponses()
        setupDataRequestUpdateManager()
    }

    @ParameterizedTest
    @MethodSource("provideParametersToCheckDataRequestUpdateUtils")
    fun `validate that all required checks are made by the data request update utils when a request patch is processed`(
        dataType: String,
        requestStatusBefore: RequestStatus,
        requestStatusAfter: RequestStatus,
    ) {
        val dataRequestEntity = testDataProvider.getDummyDataRequestEntity(dataType)
        doReturn(dataRequestEntity).whenever(mockDataRequestRepository).findByDataRequestId(dataRequestEntity.dataRequestId)
        if (requestStatusBefore != RequestStatus.Open) {
            val requestStatusEntity =
                RequestStatusEntity(
                    testDataProvider.getDummyStoredDataRequestStatusObject(dataType, requestStatusBefore),
                    dataRequestEntity,
                )
            dataRequestEntity.addToDataRequestStatusHistory(requestStatusEntity)
        }
        val dataRequestPatch = DataRequestPatch(requestStatus = requestStatusAfter)
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dataRequestEntity.dataRequestId,
            dataRequestPatch = dataRequestPatch,
            correlationId = correlationId,
        )
        verify(mockDataRequestUpdateUtils, times(1))
            .updateNotifyMeImmediatelyIfRequired(
                dataRequestPatch,
                dataRequestEntity,
            )
        verify(mockDataRequestUpdateUtils, times(1))
            .updateRequestStatusHistoryIfRequired(
                eq(dataRequestPatch),
                eq(dataRequestEntity),
                any(),
                anyOrNull(),
            )
        verify(mockDataRequestUpdateUtils, times(1))
            .updateMessageHistoryIfRequired(
                eq(dataRequestPatch),
                eq(dataRequestEntity),
                any(),
            )
        verify(mockDataRequestUpdateUtils, times(1))
            .checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                eq(dataRequestPatch),
                eq(dataRequestEntity),
            )
    }

    @Test
    fun `validate that notification behaviour is as expected when a request status is patched to answered and flag is active`() {
        val dataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Answered)
        dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
            dataRequestId = dummyDataRequestEntityWithoutEarlierQaApproval1.dataRequestId,
            dataRequestPatch = dataRequestPatch,
            correlationId = correlationId,
        )
        verify(mockRequestEmailManager, times(1))
            .sendEmailsWhenRequestStatusChanged(
                any(), eq(RequestStatus.Answered), eq(null), eq(false), eq(correlationId),
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
                any(), eq(RequestStatus.Answered), eq(null), any(), eq(correlationId),
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
                any(), eq(RequestStatus.Closed), eq(null), any(), eq(correlationId),
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
    }

    @Test
    fun `validate that answer emails for subsidiaries are sent according to flag on request status patch from open to answered`() {
        dataRequestUpdateManager.patchRequestStatusToAnsweredForParentAndSubsidiariesAndReturnParentRequestIds(
            dataMetaInformation,
            dataMetaInformation.dataId,
            correlationId,
        )
        val expectedNumberOfEmailsPerRequest = listOf(1, 0)
        for (i in 0..1) {
            verify(mockRequestEmailManager, times(expectedNumberOfEmailsPerRequest[i]))
                .sendEmailsWhenRequestStatusChanged(
                    eq(dummyDataRequestEntitiesWithoutEarlierQaApproval[i]),
                    eq(RequestStatus.Answered),
                    eq(null),
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
                    anyOrNull(),
                    eq(earlierQaApprovalExistenceInformation[i]),
                    any<String>(),
                )
        }
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
                anyOrNull(),
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
    fun `validate that patching corresponding requests for a dataset only processes the corresponding requests`() {
        dataRequestUpdateManager.patchAllRequestsToStatusNonSourceable(
            dummyNonSourceableInfo,
            correlationId,
        )
        dummyDataRequestEntitiesWithoutEarlierQaApproval.forEach {
            verify(mockDataRequestSummaryNotificationService)
                .createUserSpecificNotificationEvent(
                    eq(it), eq(RequestStatus.NonSourceable),
                    any(), any(),
                )
        }
        verifyNoMoreInteractions(mockDataRequestSummaryNotificationService)
    }
}
