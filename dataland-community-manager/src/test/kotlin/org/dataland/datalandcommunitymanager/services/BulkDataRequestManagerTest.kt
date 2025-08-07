package org.dataland.datalandcommunitymanager.services

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
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
    private lateinit var mockEntityManager: EntityManager

    @Value("\${dataland.community-manager.proxy-primary-url}")
    private val proxyPrimaryUrl: String = "dataland.com"

    private val dummyRequestId = UUID.randomUUID().toString()
    private val dummyAdminComment = "dummyAdminComment"
    private val dummyCompanyIdAndName = CompanyIdAndName(companyId = UUID.randomUUID().toString(), companyName = "Dummy Company AG")
    private val dummyReportingPeriod = "2023"
    private val dummyUserProvidedCompanyId = "companyId1"
    private val emptyList: List<DataMetaInformation> = listOf()
    private val bulkDataRequest =
        BulkDataRequest(
            companyIdentifiers = setOf(dummyUserProvidedCompanyId),
            dataTypes = setOf(DataTypeEnum.sfdr),
            reportingPeriods = setOf(dummyReportingPeriod),
        )

    @BeforeEach
    fun setUpBulkDataRequestManager() {
        mockBulkDataRequestEmailMessageBuilder = mock<BulkDataRequestEmailMessageBuilder>()
        mockDataRequestProcessingUtils = createDataRequestProcessingUtilsMock()
        mockMetaDataController = mock<MetaDataControllerApi>()
        mockEntityManager = mock<EntityManager>()

        bulkDataRequestManager =
            BulkDataRequestManager(
                mock<DataRequestLogger>(),
                mockBulkDataRequestEmailMessageBuilder,
                mockDataRequestProcessingUtils,
                mockMetaDataController,
                mockEntityManager,
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

    private fun assertResponse(
        expectedAccepted: List<ResourceResponse>? = emptyList(),
        expectedAlreadyExistingRequests: List<ResourceResponse>? = emptyList(),
        expectedAlreadyExistingDatasets: List<ResourceResponse>? = emptyList(),
        expectedRejected: List<String>? = emptyList(),
    ) {
        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(expectedAccepted, response.acceptedDataRequests)
        assertEquals(expectedAlreadyExistingRequests, response.alreadyExistingRequests)
        assertEquals(expectedAlreadyExistingDatasets, response.alreadyExistingDatasets)
        assertEquals(expectedRejected, response.rejectedCompanyIdentifiers)
    }

    private fun setupEmptyMocks() {
        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(mapOf(dummyUserProvidedCompanyId to dummyCompanyIdAndName), emptyList()))
        whenever(mockMetaDataController.retrieveMetaDataOfActiveDatasets(any())).thenReturn(emptyList)
        val mockQuery = mock<Query>()
        whenever(mockEntityManager.createNativeQuery(any(), any<Class<DataRequestEntity>>()))
            .thenReturn(mock<Query>())
        whenever(mockQuery.resultList).thenReturn(emptyList<DataRequestEntity>())
    }

    @Test
    fun `process bulk data request`() {
        setupEmptyMocks()

        val expectedAcceptedDataRequest =
            listOf(
                ResourceResponse(
                    userProvidedIdentifier = dummyUserProvidedCompanyId,
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = dummyReportingPeriod,
                    resourceId = dummyRequestId,
                    resourceUrl = "https://dataland.com/requests/$dummyRequestId",
                ),
            )

        assertResponse(expectedAccepted = expectedAcceptedDataRequest)
    }

    @Test
    fun `process bulk data request with existing request`() {
        setupEmptyMocks()

        val expectedAlreadyExistingDataRequest =
            listOf(
                ResourceResponse(
                    userProvidedIdentifier = dummyUserProvidedCompanyId,
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = dummyReportingPeriod,
                    resourceId = dummyRequestId,
                    resourceUrl = "https://dataland.com/requests/$dummyRequestId",
                ),
            )
        val dataRequestEntity =
            DataRequestEntity(
                dataRequestId = dummyRequestId,
                userId = dummyUserProvidedCompanyId,
                creationTimestamp = System.currentTimeMillis(),
                dataType = "sfdr",
                reportingPeriod = dummyReportingPeriod,
                datalandCompanyId = dummyCompanyIdAndName.companyId,
                notifyMeImmediately = false,
                messageHistory = emptyList(),
                dataRequestStatusHistory = emptyList(),
                lastModifiedDate = System.currentTimeMillis(),
                requestPriority = RequestPriority.Low,
                adminComment = dummyAdminComment,
            )

        val mockQuery = mock<Query>()
        whenever(mockQuery.resultList).thenReturn(listOf(dataRequestEntity))
        whenever(mockEntityManager.createNativeQuery(any(), any<Class<DataRequestEntity>>()))
            .thenReturn(mockQuery)

        assertResponse(expectedAlreadyExistingRequests = expectedAlreadyExistingDataRequest)
    }

    @Test
    fun `process bulk data request with existing data`() {
        setupEmptyMocks()
        whenever(mockMetaDataController.retrieveMetaDataOfActiveDatasets(any())).thenReturn(
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
            ),
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

        assertResponse(expectedAlreadyExistingDatasets = expectedAlreadyExistingDataSetsResponse)
    }

    @Test
    fun `process bulk data request with no valid company identifiers data`() {
        setupEmptyMocks()

        whenever(mockDataRequestProcessingUtils.performIdentifierValidation(anyList()))
            .thenReturn(Pair(emptyMap(), listOf(dummyUserProvidedCompanyId)))

        val expectedRejectedCompanyIdentifiers = listOf(dummyUserProvidedCompanyId)

        assertResponse(expectedRejected = expectedRejectedCompanyIdentifiers)
    }
}
