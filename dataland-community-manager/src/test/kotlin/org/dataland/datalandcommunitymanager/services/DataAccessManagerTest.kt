package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.AccessRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.anySet
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

class DataAccessManagerTest {

    private lateinit var dataAccessManager: DataAccessManager

    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var dataRequestLogger: DataRequestLogger
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var accessRequestLogger: AccessRequestLogger

    private lateinit var authenticationMock: DatalandJwtAuthentication

    private var companyId = "companyId"
    private var userId = "userId"

    private var revokedAccessReportingYear = "2023"
    private var grantedAccessReportingYear = "2022"
    private var noRequestReportingYear = "2021"

    private fun setupVsmeRequestWithGrantedAccess(): List<DataRequestEntity> {
        val dataRequest = DataRequestEntity(
            userId = userId,
            dataType = DataTypeEnum.vsme.toString(),
            reportingPeriod = grantedAccessReportingYear,
            creationTimestamp = 0,
            datalandCompanyId = companyId,
        )

        val requestStatus1 = RequestStatusEntity(
            UUID.randomUUID().toString(), RequestStatus.Answered, AccessStatus.Pending, 0, dataRequest,
        )
        val requestStatus2 = RequestStatusEntity(
            UUID.randomUUID().toString(), RequestStatus.Answered, AccessStatus.Granted, 1, dataRequest,
        )
        dataRequest.dataRequestStatusHistory = listOf(requestStatus1, requestStatus2).shuffled()

        return listOf(dataRequest)
    }

    private fun setupVsmeRequestWithRevokedAccess(): List<DataRequestEntity> {
        val dataRequest = DataRequestEntity(
            userId = userId,
            dataType = DataTypeEnum.vsme.toString(),
            reportingPeriod = revokedAccessReportingYear,
            creationTimestamp = 0,
            datalandCompanyId = companyId,
        )

        val requestStatus1 = RequestStatusEntity(
            UUID.randomUUID().toString(), RequestStatus.Answered, AccessStatus.Pending, 0, dataRequest,
        )
        val requestStatus2 = RequestStatusEntity(
            UUID.randomUUID().toString(), RequestStatus.Answered, AccessStatus.Granted, 1, dataRequest,
        )
        val requestStatus3 = RequestStatusEntity(
            UUID.randomUUID().toString(), RequestStatus.Answered, AccessStatus.Revoked, 2, dataRequest,
        )
        dataRequest.dataRequestStatusHistory = listOf(requestStatus1, requestStatus2, requestStatus3).shuffled()

        return listOf(dataRequest)
    }

    @BeforeEach
    fun setup() {
        mockDataRequestRepository = createDataRequestRepository()

        accessRequestLogger = mock(AccessRequestLogger::class.java)
        dataRequestLogger = mock(DataRequestLogger::class.java)
        mockDataRequestProcessingUtils = createRequestProcessingUtils()

        dataAccessManager = DataAccessManager(
            mockDataRequestRepository, dataRequestLogger, mockDataRequestProcessingUtils, accessRequestLogger,
        )
    }

    private fun createRequestProcessingUtils(): DataRequestProcessingUtils {
        val dataRequestProcessingUtils = mock(DataRequestProcessingUtils::class.java)
        doNothing().`when`(dataRequestProcessingUtils).addNewRequestStatusToHistory(
            any(DataRequestEntity::class.java), any(RequestStatus::class.java),
            any(AccessStatus::class.java), any(Long::class.java),
        )
        doNothing().`when`(dataRequestProcessingUtils).addNewMessageToHistory(
            any(DataRequestEntity::class.java), anySet(), anyString(), any(Long::class.java),
        )
        return dataRequestProcessingUtils
    }

    private fun createDataRequestRepository(): DataRequestRepository {
        val dataRequestRepository = mock(DataRequestRepository::class.java)
        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, DataTypeEnum.vsme.toString(), grantedAccessReportingYear,
            ),
        ).thenReturn(setupVsmeRequestWithGrantedAccess())

        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, DataTypeEnum.vsme.toString(), revokedAccessReportingYear,
            ),
        ).thenReturn(setupVsmeRequestWithRevokedAccess())

        `when`(
            dataRequestRepository.findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, DataTypeEnum.vsme.toString(), noRequestReportingYear,
            ),
        ).thenReturn(listOf())
        return dataRequestRepository
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
    fun `validate that public datasets are always accessible`() {
        val dataTypes = listOf(
            DataTypeEnum.lksg, DataTypeEnum.p2p, DataTypeEnum.sfdr,
            DataTypeEnum.eutaxonomyMinusFinancials, DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
            DataTypeEnum.esgMinusQuestionnaire, DataTypeEnum.heimathafen,
        )

        dataTypes.forEach {
            assertDoesNotThrow {
                dataAccessManager.headAccessToDataset(companyId, grantedAccessReportingYear, it.toString(), userId)
            }
        }
    }

    @Test
    fun `validate exception is thrown when datatype is unknown`() {
        assertThrows(InvalidInputApiException::class.java) {
            dataAccessManager.headAccessToDataset(
                companyId, revokedAccessReportingYear, "123562134", userId,
            )
        }
    }

    @Test
    fun `validate that vsme dataset is not accessible with no access`() {
        assertThrows(ResourceNotFoundApiException::class.java) {
            dataAccessManager.headAccessToDataset(
                companyId, revokedAccessReportingYear, DataTypeEnum.vsme.toString(), userId,
            )
        }
    }

    @Test
    fun `validate that vsme dataset is accessible with granted access`() {
        assertDoesNotThrow {
            dataAccessManager.headAccessToDataset(
                companyId, grantedAccessReportingYear, DataTypeEnum.vsme.toString(), userId,
            )
        }
    }

    @Test
    fun `validate that accessStatus is set to pending for existing data request`() {
        dataAccessManager.createAccessRequestToPrivateDataset(
            userId, companyId, DataTypeEnum.vsme, revokedAccessReportingYear, null, null,
        )

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(DataRequestEntity::class.java), any(RequestStatus::class.java),
                eq(AccessStatus.Pending), any(Long::class.java),
            )
        verify(mockDataRequestProcessingUtils, times(0))
            .addNewMessageToHistory(
                any(DataRequestEntity::class.java), anySet(), anyString(), any(Long::class.java),
            )

        val saveCaptor = ArgumentCaptor.forClass(DataRequestEntity::class.java)
        verify(mockDataRequestRepository, times(1))
            .save(capture(saveCaptor))

        assertEquals(0, saveCaptor.value.creationTimestamp)
    }

    @Test
    fun `validate that new data request is stored with pending accessStatus when no data request exists`() {
        val message = "message"
        val contacts = setOf("test@example.com", "test2@example.com")

        val currentTime = Instant.now().toEpochMilli()

        dataAccessManager.createAccessRequestToPrivateDataset(
            userId, companyId, DataTypeEnum.vsme, noRequestReportingYear, contacts, message,
        )

        verify(mockDataRequestProcessingUtils, times(1))
            .addNewRequestStatusToHistory(
                any(DataRequestEntity::class.java), any(RequestStatus::class.java),
                eq(AccessStatus.Pending), any(Long::class.java),
            )
        verify(mockDataRequestProcessingUtils, times(1))
            .addNewMessageToHistory(
                any(DataRequestEntity::class.java), eq(contacts), eq(message), any(Long::class.java),
            )

        val saveCaptor = ArgumentCaptor.forClass(DataRequestEntity::class.java)
        verify(mockDataRequestRepository, times(1))
            .save(capture(saveCaptor))

        assert(saveCaptor.value.creationTimestamp >= currentTime)
    }

// TODO review comment why not import those?
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
    private fun <T> eq(value: T): T = ArgumentMatchers.eq(value) ?: value
    private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}
