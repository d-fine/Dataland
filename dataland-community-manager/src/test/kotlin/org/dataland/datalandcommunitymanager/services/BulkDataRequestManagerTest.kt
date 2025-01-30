package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.DataSetsResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
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
    private val dummyCompanyIdAndName = CompanyIdAndName(companyIdRegexSafeCompanyId, "Dummy Company AG")

    @BeforeEach
    fun setUpBulkDataRequestManager() {
        mockBulkDataRequestEmailMessageSender = mock(BulkDataRequestEmailMessageSender::class.java)
        mockDataRequestProcessingUtils = createDataRequestProcessingUtilsMock()
        mockMetaDataController = createMetaDataControllerMock()

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

    private fun createMetaDataControllerMock(): MetaDataControllerApi {
        val metaDataControllerMock = mock(MetaDataControllerApi::class.java)

        `when`(metaDataControllerMock.postListOfDataMetaInfoRequests(anyList())).thenAnswer {
            val dataMetaInformationList =
                listOf(
                    DataMetaInformation(
                        dataId = "dataId1",
                        companyId = dummyCompanyIdAndName.companyId,
                        dataType = DataTypeEnum.sfdr,
                        uploadTime = System.currentTimeMillis(),
                        reportingPeriod = "2023-Q1",
                        currentlyActive = true,
                        qaStatus = QaStatus.Accepted,
                        url = "https://example.com/dataId1",
                    ),
                )
            return@thenAnswer dataMetaInformationList
        }

        return metaDataControllerMock
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
                dataRequestId = "request-id",
                datalandCompanyId = it.arguments[0] as String,
                reportingPeriod = it.arguments[2] as String,
                creationTimestamp = 0,
                lastModifiedDate = 0,
                dataType = (it.arguments[1] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                dataRequestStatusHistory = emptyList(),
                userId = "user-id",
                requestPriority = RequestPriority.Low,
                adminComment = "dummyAdminComment",
            )
        }
        `when`(utilsMock.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(dummyCompanyIdAndName)
        return utilsMock
    }

    @Test
    fun `process bulk data request with no existing data`() {
        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf("companyId1"),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf("2023-Q1"),
            )

        val expectedAcceptedDataRequest =
            listOf(
                DataRequestResponse(
                    userProvidedCompanyId = "companyId1",
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = "2023-Q1",
                    requestId = "request-id",
                    requestUrl = "https://dataland.com/requests/request-id",
                ),
            )
        val emptyList: List<DataMetaInformation> = listOf()

        whenever(mockMetaDataController.postListOfDataMetaInfoRequests(any())).thenReturn(emptyList)

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        assertEquals(expectedAcceptedDataRequest, response.acceptedDataRequests)
    }

    @Test
    fun `process bulk data request with existing data`() {
        val bulkDataRequest =
            BulkDataRequest(
                companyIdentifiers = setOf("companyId1"),
                dataTypes = setOf(DataTypeEnum.sfdr),
                reportingPeriods = setOf("2023-Q1"),
            )

        val expectedAlreadyExistingDataSetsResponse =
            listOf(
                DataSetsResponse(
                    userProvidedCompanyId = "companyId1",
                    companyName = dummyCompanyIdAndName.companyName,
                    framework = "sfdr",
                    reportingPeriod = "2023-Q1",
                    datasetId = "dataId1",
                    datasetUrl = "https://example.com/dataId1",
                ),
            )

        val response = bulkDataRequestManager.processBulkDataRequest(bulkDataRequest)
        print(response.alreadyExistingDataSets)
        assertEquals(expectedAlreadyExistingDataSetsResponse, response.alreadyExistingDataSets)
    }
}
