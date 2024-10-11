package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.anySet
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

class DataAccessManagerTest {
    private lateinit var dataAccessManager: DataAccessManager

    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var dataRequestLogger: DataRequestLogger
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils

    private lateinit var authenticationMock: DatalandJwtAuthentication

    private val companyId = "companyId"
    private val userId = "userId"

    private val revokedAccessReportingYear = "2023"
    private val grantedAccessReportingYear = "2022"
    private val noRequestReportingYear = "2021"

    private fun setupVsmeRequestWithGrantedAccess(): List<DataRequestEntity> {
        val dataRequest =
            DataRequestEntity(
                userId = userId,
                dataType = DataTypeEnum.vsme.toString(),
                reportingPeriod = grantedAccessReportingYear,
                creationTimestamp = 0,
                datalandCompanyId = companyId,
            )

        val requestStatus1 =
            RequestStatusEntity(
                statusHistoryId = UUID.randomUUID().toString(), requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Pending, creationTimestamp = 0, dataRequest = dataRequest,
            )
        val requestStatus2 =
            RequestStatusEntity(
                statusHistoryId = UUID.randomUUID().toString(), requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Granted, creationTimestamp = 1, dataRequest = dataRequest,
            )
        dataRequest.dataRequestStatusHistory = listOf(requestStatus1, requestStatus2).shuffled()

        return listOf(dataRequest)
    }

    private fun setupVsmeRequestWithRevokedAccess(): List<DataRequestEntity> {
        val dataRequest =
            DataRequestEntity(
                userId = userId,
                dataType = DataTypeEnum.vsme.toString(),
                reportingPeriod = revokedAccessReportingYear,
                creationTimestamp = 0,
                datalandCompanyId = companyId,
            )

        val requestStatus1 =
            RequestStatusEntity(
                statusHistoryId = UUID.randomUUID().toString(), requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Pending, creationTimestamp = 0, dataRequest = dataRequest,
            )
        val requestStatus2 =
            RequestStatusEntity(
                statusHistoryId = UUID.randomUUID().toString(), requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Granted, creationTimestamp = 1, dataRequest = dataRequest,
            )
        val requestStatus3 =
            RequestStatusEntity(
                statusHistoryId = UUID.randomUUID().toString(), requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Revoked, creationTimestamp = 2, dataRequest = dataRequest,
            )
        dataRequest.dataRequestStatusHistory = listOf(requestStatus1, requestStatus2, requestStatus3).shuffled()

        return listOf(dataRequest)
    }

    @BeforeEach
    fun setup() {
        mockDataRequestRepository = createDataRequestRepository()

        dataRequestLogger = mock(DataRequestLogger::class.java)
        mockDataRequestProcessingUtils = createRequestProcessingUtils()

        dataAccessManager =
            DataAccessManager(
                dataRequestRepository = mockDataRequestRepository, dataRequestLogger = dataRequestLogger,
                dataRequestProcessingUtils = mockDataRequestProcessingUtils,
            )
    }

    private fun createRequestProcessingUtils(): DataRequestProcessingUtils {
        val dataRequestProcessingUtils = mock(DataRequestProcessingUtils::class.java)
        doNothing().`when`(dataRequestProcessingUtils).addNewRequestStatusToHistory(
            dataRequestEntity = any(), requestStatus = any(),
            accessStatus = any(), modificationTime = any(),
        )
        doNothing().`when`(dataRequestProcessingUtils).addMessageToMessageHistory(
            dataRequestEntity = any(), contacts = anySet(), message = anyString(),
            modificationTime = any(),
        )
        return dataRequestProcessingUtils
    }

    private fun createDataRequestRepository(): DataRequestRepository {
        val dataRequestRepository = mock(DataRequestRepository::class.java)
        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId = userId, datalandCompanyId = companyId, dataType = DataTypeEnum.vsme.toString(),
                reportingPeriod = grantedAccessReportingYear,
            ),
        ).thenReturn(setupVsmeRequestWithGrantedAccess())

        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId = userId, datalandCompanyId = companyId, dataType = DataTypeEnum.vsme.toString(),
                reportingPeriod = revokedAccessReportingYear,
            ),
        ).thenReturn(setupVsmeRequestWithRevokedAccess())

        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId = userId, datalandCompanyId = companyId, dataType = DataTypeEnum.vsme.toString(),
                reportingPeriod = noRequestReportingYear,
            ),
        ).thenReturn(listOf())
        return dataRequestRepository
    }

    @BeforeEach
    fun setupSecurityMock() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                username = "user@example.com",
                userId = "1234-221-1111elf",
                roles = setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that public datasets are always accessible`() {
        val dataTypes =
            listOf(
                DataTypeEnum.lksg, DataTypeEnum.p2p, DataTypeEnum.sfdr,
                DataTypeEnum.eutaxonomyMinusFinancials, DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                DataTypeEnum.esgMinusQuestionnaire, DataTypeEnum.heimathafen,
            )

        dataTypes.forEach {
            assertDoesNotThrow {
                dataAccessManager.hasAccessToDataset(companyId, grantedAccessReportingYear, it.toString(), userId)
            }
        }
    }

    @Test
    fun `validate exception is thrown when datatype is unknown`() {
        assertThrows(InvalidInputApiException::class.java) {
            dataAccessManager.hasAccessToDataset(
                companyId = companyId, reportingPeriod = revokedAccessReportingYear, dataType = "123562134",
                userId = userId,
            )
        }
    }

    @Test
    fun `validate that vsme dataset is not accessible with no access`() {
        assertThrows(ResourceNotFoundApiException::class.java) {
            dataAccessManager.hasAccessToDataset(
                companyId = companyId, reportingPeriod = revokedAccessReportingYear,
                dataType = DataTypeEnum.vsme.toString(), userId = userId,
            )
        }
    }

    @Test
    fun `validate that vsme dataset is accessible with granted access`() {
        assertDoesNotThrow {
            dataAccessManager.hasAccessToDataset(
                companyId = companyId, reportingPeriod = grantedAccessReportingYear,
                dataType = DataTypeEnum.vsme.toString(), userId = userId,
            )
        }
    }

    @Test
    fun `validate that accessStatus is set to pending for existing data request`() {
        dataAccessManager.createAccessRequestToPrivateDataset(
            userId = userId, companyId = companyId, dataType = DataTypeEnum.vsme,
            reportingPeriod = revokedAccessReportingYear, contacts = null, message = null,
        )

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                dataRequestEntity = any(), requestStatus = any(),
                accessStatus = eq(AccessStatus.Pending), modificationTime = any(),
            )
        verify(mockDataRequestProcessingUtils, times(0))
            .addMessageToMessageHistory(
                dataRequestEntity = any(), contacts = anySet(), message = anyString(),
                modificationTime = any(),
            )

        verify(mockDataRequestRepository, times(1))
            .save(argThat { creationTimestamp == 0L })
    }

    @Test
    fun `validate that new data request is stored with pending accessStatus when no data request exists`() {
        val message = "message"
        val contacts = setOf("test@example.com", "test2@example.com")

        val currentTime = Instant.now().toEpochMilli()

        dataAccessManager.createAccessRequestToPrivateDataset(
            userId = userId, companyId = companyId, dataType = DataTypeEnum.vsme,
            reportingPeriod = noRequestReportingYear, contacts = contacts, message = message,
        )

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                dataRequestEntity = any(), requestStatus = any(),
                accessStatus = eq(AccessStatus.Pending), modificationTime = any(),
            )
        verify(mockDataRequestProcessingUtils, times(1))
            .addMessageToMessageHistory(
                dataRequestEntity = any(), contacts = eq(contacts), message = eq(message),
                modificationTime = any(),
            )

        verify(mockDataRequestRepository, times(1))
            .save(argThat { creationTimestamp >= currentTime })
    }
}
