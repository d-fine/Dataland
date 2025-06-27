package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.ResourceResponse
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import java.util.UUID

class BulkDataRequestManagerTest {
    private lateinit var bulkDataRequestManager: BulkDataRequestManager
    private lateinit var mockBulkDataRequestEmailMessageBuilder: BulkDataRequestEmailMessageBuilder
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockMetaDataController: MetaDataControllerApi

    @Value("\${dataland.community-manager.proxy-primary-url}")
    private val proxyPrimaryUrl: String = "dataland.com"

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()
    private val dummyRequestId = "request-id"
    private val dummyAdminComment = "dummyAdminComment"
    private val dummyCompanyIdAndName = CompanyIdAndName(companyIdRegexSafeCompanyId, "Dummy Company AG")
    private val dummyReportingPeriod = "2023"
    private val dummyUserProvidedCompanyId = "companyId1"

    @BeforeEach
    fun setUpBulkDataRequestManager() {
        mockBulkDataRequestEmailMessageBuilder = mock<BulkDataRequestEmailMessageBuilder>()
        mockDataRequestProcessingUtils = createDataRequestProcessingUtilsMock()
        mockMetaDataController = mock<MetaDataControllerApi>()

        bulkDataRequestManager =
            BulkDataRequestManager(
                mock<DataRequestLogger>(),
                mockBulkDataRequestEmailMessageBuilder,
                mockDataRequestProcessingUtils,
                mockMetaDataController,
                proxyPrimaryUrl,
            )
        TestUtils.mockSecurityContext("requester@bigplayer.com", "1234-221-1111elf", DatalandRealmRole.ROLE_USER)
    }

    private fun createDataRequestProcessingUtilsMock(): DataRequestProcessingUtils {
        val utilsMock = mock<DataRequestProcessingUtils>()
        whenever(
            utilsMock.storeDataRequestEntityAsOpen(
                userId = anyString(),
                datalandCompanyId = anyString(),
                dataType = any(),
                notifyMeImmediately = any(),
                reportingPeriod = anyString(),
                contacts = anyOrNull(),
                message = anyOrNull(),
            ),
        ).thenAnswer {
            DataRequestEntity(
                dataRequestId = dummyRequestId,
                datalandCompanyId = it.arguments[1] as String,
                notifyMeImmediately = it.arguments[3] as Boolean,
                reportingPeriod = it.arguments[4] as String,
                creationTimestamp = 0,
                lastModifiedDate = 0,
                dataType = (it.arguments[2] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                dataRequestStatusHistory = emptyList(),
                userId = it.arguments[0] as String,
                requestPriority = RequestPriority.Low,
                adminComment = dummyAdminComment,
            )
        }
        return utilsMock
    }

    @Test
    fun `process bulk data request`() {
        val emptyList: List<DataMetaInformation> = listOf()
        whenever(mockMetaDataController.postListOfDataMetaInfoFilters(any())).thenReturn(emptyList)
        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(mapOf(dummyUserProvidedCompanyId to dummyCompanyIdAndName), emptyList()))

        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf(dummyUserProvidedCompanyId),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf(dummyReportingPeriod),
            )

        val expectedAcceptedDataRequest =
            listOf(
                ResourceResponse(
                    userProvidedIdentifier = dummyUserProvidedCompanyId,
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = dummyReportingPeriod,
                    resourceId = dummyRequestId,
                    resourceUrl = "https://dataland.com/requests/request-id",
                ),
            )

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(expectedAcceptedDataRequest, response.acceptedDataRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingNonFinalRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingDatasets)
        assertEquals(emptyList<String>(), response.rejectedCompanyIdentifiers)
    }

    @Test
    fun `process bulk data request with existing request`() {
        val emptyList: List<DataMetaInformation> = listOf()
        whenever(mockMetaDataController.postListOfDataMetaInfoFilters(any())).thenReturn(emptyList)
        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(mapOf(dummyUserProvidedCompanyId to dummyCompanyIdAndName), emptyList()))
        whenever(mockDataRequestProcessingUtils.getRequestIdForDataRequestWithNonFinalStatus(any(), any(), any()))
            .thenReturn(dummyRequestId)

        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf(dummyUserProvidedCompanyId),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf(dummyReportingPeriod),
            )

        val expectedAlreadyExistingDataRequest =
            listOf(
                ResourceResponse(
                    userProvidedIdentifier = dummyUserProvidedCompanyId,
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = dummyReportingPeriod,
                    resourceId = dummyRequestId,
                    resourceUrl = "https://dataland.com/requests/request-id",
                ),
            )

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(emptyList<ResourceResponse>(), response.acceptedDataRequests)
        assertEquals(expectedAlreadyExistingDataRequest, response.alreadyExistingNonFinalRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingDatasets)
        assertEquals(emptyList<String>(), response.rejectedCompanyIdentifiers)
    }

    @Test
    fun `process bulk data request with existing data`() {
        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(mapOf(dummyUserProvidedCompanyId to dummyCompanyIdAndName), emptyList()))
        whenever(mockMetaDataController.postListOfDataMetaInfoFilters(anyList())).thenAnswer {
            val dataMetaInformationList =
                listOf(
                    DataMetaInformation(
                        dataId = "dataId1",
                        companyId = dummyCompanyIdAndName.companyId,
                        dataType = DataTypeEnum.sfdr,
                        uploadTime = System.currentTimeMillis(),
                        reportingPeriod = dummyReportingPeriod,
                        currentlyActive = true,
                        qaStatus = QaStatus.Accepted,
                        ref = "https://example.com/dataId1",
                    ),
                )
            dataMetaInformationList
        }

        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf(dummyUserProvidedCompanyId),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf(dummyReportingPeriod),
            )

        val expectedAlreadyExistingDataSetsResponse =
            listOf(
                ResourceResponse(
                    userProvidedIdentifier = dummyUserProvidedCompanyId,
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = dummyReportingPeriod,
                    resourceId = "dataId1",
                    resourceUrl = "https://example.com/dataId1",
                ),
            )

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(emptyList<ResourceResponse>(), response.acceptedDataRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingNonFinalRequests)
        assertEquals(expectedAlreadyExistingDataSetsResponse, response.alreadyExistingDatasets)
        assertEquals(emptyList<String>(), response.rejectedCompanyIdentifiers)
    }

    @Test
    fun `process bulk data request with no valid company identifiers data`() {
        val emptyList: List<DataMetaInformation> = listOf()

        whenever(mockMetaDataController.postListOfDataMetaInfoFilters(any())).thenReturn(emptyList)
        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(emptyMap(), listOf(dummyUserProvidedCompanyId)))

        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf(dummyUserProvidedCompanyId),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf(dummyReportingPeriod),
            )

        val expectedRejectedCompanyIdentifiers =
            listOf(dummyUserProvidedCompanyId)

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(emptyList<ResourceResponse>(), response.acceptedDataRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingNonFinalRequests)
        assertEquals(emptyList<ResourceResponse>(), response.alreadyExistingDatasets)
        assertEquals(expectedRejectedCompanyIdentifiers, response.rejectedCompanyIdentifiers)
    }
}
