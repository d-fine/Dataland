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
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class BulkDataRequestManagerTest {
    private lateinit var bulkDataRequestManager: BulkDataRequestManager
    private lateinit var mockBulkDataRequestEmailMessageSender: BulkDataRequestEmailMessageSender
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockMetaDataController: MetaDataControllerApi
    private lateinit var mockAuthentication: DatalandJwtAuthentication

    @Value("\${dataland.community-manager.proxy-primary-url}")
    private val proxyPrimaryUrl: String = "dataland.com"

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()
    private val dummyRequestId = "request-id"
    private val dummyUserId = "user-id"
    private val dummyAdminComment = "dummyAdminComment"
    private val dummyCompanyIdAndName = CompanyIdAndName(companyIdRegexSafeCompanyId, "Dummy Company AG")
    private val dummyReportingPeriod = "2023-Q1"
    private val dummyUserProvidedCompanyId = "companyId1"

    @BeforeEach
    fun setUpBulkDataRequestManager() {
        mockBulkDataRequestEmailMessageSender = mock(BulkDataRequestEmailMessageSender::class.java)
        mockDataRequestProcessingUtils = createDataRequestProcessingUtilsMock()
        mockMetaDataController = mock(MetaDataControllerApi::class.java)

        bulkDataRequestManager =
            BulkDataRequestManager(
                mock(DataRequestLogger::class.java),
                mockBulkDataRequestEmailMessageSender,
                mockDataRequestProcessingUtils,
                mockMetaDataController,
                proxyPrimaryUrl,
            )
        val mockSecurityContext = createSecurityContextMock()
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun createSecurityContextMock(): SecurityContext {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "requester@bigplayer.com",
                "1234-221-1111elf",
                setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        return mockSecurityContext
    }

    private fun createDataRequestProcessingUtilsMock(): DataRequestProcessingUtils {
        val utilsMock = mock(DataRequestProcessingUtils::class.java)
        `when`(
            utilsMock.storeDataRequestEntityAsOpen(
                anyString(),
                any(),
                anyString(),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenAnswer {
            DataRequestEntity(
                dataRequestId = dummyRequestId,
                datalandCompanyId = it.arguments[0] as String,
                reportingPeriod = it.arguments[2] as String,
                creationTimestamp = 0,
                lastModifiedDate = 0,
                dataType = (it.arguments[1] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                dataRequestStatusHistory = emptyList(),
                userId = dummyUserId,
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
        `when`(mockDataRequestProcessingUtils.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(dummyCompanyIdAndName)

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
        `when`(mockDataRequestProcessingUtils.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(dummyCompanyIdAndName)
        `when`(mockDataRequestProcessingUtils.getRequestIdForDataRequestWithNonFinalStatus(anyString(), any(), anyString()))
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
        `when`(mockDataRequestProcessingUtils.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(dummyCompanyIdAndName)
        `when`(mockMetaDataController.postListOfDataMetaInfoFilters(anyList())).thenAnswer {
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
                        url = "https://example.com/dataId1",
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
        `when`(mockDataRequestProcessingUtils.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(null)

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
