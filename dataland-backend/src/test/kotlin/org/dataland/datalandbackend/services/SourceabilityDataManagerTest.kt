package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.repositories.SourceabilityDataRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
class SourceabilityDataManagerTest(
    @Autowired private val sourceabilityDataRepository: SourceabilityDataRepository,
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val objectMapper: ObjectMapper,
) {
    lateinit var sourceabilityDataManager: SourceabilityDataManager
    private val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
    private val mockCompanyQueryManager = mock(CompanyQueryManager::class.java)
    private val existingCompanyId = "existingCompanyId"
    private val dataType = DataType("eutaxonomy-financials")
    private val reportingPeriod = "2023"
    private val mockCloudEventMessageHandler: CloudEventMessageHandler =
        mock(CloudEventMessageHandler::class.java)

    @BeforeEach
    fun setup() {
        Mockito.reset(mockDataMetaInformationManager, mockCompanyQueryManager, mockCloudEventMessageHandler)
        sourceabilityDataManager =
            SourceabilityDataManager(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                objectMapper = objectMapper,
                dataMetaInformationManager = mockDataMetaInformationManager,
                sourceabilityDataRepository = sourceabilityDataRepository,
                nonSourceabilityDataRepository = nonSourceabilityDataRepository,
                companyQueryManager = mockCompanyQueryManager,
            )
        doNothing().whenever(mockCompanyQueryManager).assertCompanyIdExists(any())
        whenever(mockDataMetaInformationManager.searchDataMetaInfo(any())).thenReturn(emptyList())

        sourceabilityDataRepository.deleteAll()
        nonSourceabilityDataRepository.deleteAll()

        mockSecurityContext("testUserId", setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER))

        val sourceabilityEntity =
            SourceabilityEntity(
                eventId = null,
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = false,
                reason = "Uploaded by a user with the Id:testUploaderId",
                creationTime = Instant.now().toEpochMilli(),
                userId = "testUploaderId",
            )
        sourceabilityDataRepository.save(sourceabilityEntity)
    }

    @Test
    fun `check that an exception is thrown when non existing companyId is provided processing sourceability storage`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        doThrow(ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $nonExistingCompanyId"))
            .whenever(mockCompanyQueryManager)
            .assertCompanyIdExists(nonExistingCompanyId)

        val sourceabilityInfo =
            SourceabilityInfo(nonExistingCompanyId, dataType, "2023", true, "test reason")
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                sourceabilityDataManager.processSourceabilityDataStorageRequest(
                    sourceabilityInfo,
                )
            }
        assertEquals(
            "Dataland does not know the company ID $nonExistingCompanyId",
            thrown.message,
        )
    }

    @Test
    fun `check that processSourceabilityDataStorageRequest stores canonical pending non-sourceability entry`() {
        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = "2026",
                isNonSourceable = true,
                reason = "Test reason",
            )

        sourceabilityDataManager.processSourceabilityDataStorageRequest(sourceabilityInfo)

        verify(mockDataMetaInformationManager).searchDataMetaInfo(
            DataMetaInformationSearchFilter(
                companyId = sourceabilityInfo.companyId,
                dataType = sourceabilityInfo.dataType,
                onlyActive = false,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                qaStatus = QaStatus.Accepted,
            ),
        )
        val created =
            nonSourceabilityDataRepository
                .findAllByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
                    sourceabilityInfo.companyId,
                    sourceabilityInfo.dataType,
                    sourceabilityInfo.reportingPeriod,
                ).first()
        assertEquals(QaStatus.Pending, created.qaStatus)
        assertFalse(created.currentlyActive)

        verify(mockCloudEventMessageHandler, times(2)).buildCEMessageAndSendToQueue(
            any(), any(), any(), any(), any(),
        )
    }

    @Test
    fun `check that duplicate tuple with pending or accepted qa status is rejected`() {
        nonSourceabilityDataRepository.save(
            NonSourceabilityInformationEntity(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                qaStatus = QaStatus.Pending,
                uploaderUserId = "testUser",
                uploadTime = Instant.now().toEpochMilli(),
                currentlyActive = false,
                reason = "already requested",
                bypassQa = false,
            ),
        )

        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = true,
                reason = "duplicate",
            )

        assertThrows<InvalidInputApiException> {
            sourceabilityDataManager.processSourceabilityDataStorageRequest(sourceabilityInfo)
        }
    }

    @Test
    fun `check that bypassQa requires admin role`() {
        mockSecurityContext("testUserId", setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER))

        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = "2027",
                isNonSourceable = true,
                reason = "requires bypass",
            )

        assertThrows<AccessDeniedException> {
            sourceabilityDataManager.processSourceabilityDataStorageRequest(sourceabilityInfo, bypassQa = true)
        }
    }

    @Test
    fun `check that a sourceable dataset is stored to legacy backup repository`() {
        val sourceableReportingPeriod = "2028"
        val uploaderId = "testUploaderId"

        sourceabilityDataManager.storeSourceableData(
            companyId = existingCompanyId,
            dataType = dataType,
            reportingPeriod = sourceableReportingPeriod,
            uploaderId = uploaderId,
        )

        val createdBackupEntry =
            sourceabilityDataRepository
                .searchNonSourceableData(
                    org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter(
                        companyId = existingCompanyId,
                        dataType = dataType,
                        reportingPeriod = sourceableReportingPeriod,
                        nonSourceable = false,
                    ),
                ).first()
        assertEquals(existingCompanyId, createdBackupEntry.companyId)
        assertEquals(sourceableReportingPeriod, createdBackupEntry.reportingPeriod)
        assertEquals(dataType, createdBackupEntry.dataType)
        assertEquals(false, createdBackupEntry.isNonSourceable)
    }

    @Test
    fun `check that runtime reads use canonical model and ignore legacy backup rows`() {
        sourceabilityDataRepository.save(
            SourceabilityEntity(
                eventId = null,
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = "2090",
                isNonSourceable = true,
                reason = "legacy only row",
                creationTime = Instant.now().toEpochMilli(),
                userId = "legacy-user",
            ),
        )

        val canonicalResults =
            sourceabilityDataManager.getSourceabilityDataByFilters(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = "2090",
                nonSourceable = true,
            )

        assertTrue(canonicalResults.isEmpty())
    }

    private fun mockSecurityContext(
        userId: String,
        roles: Set<DatalandRealmRole>,
    ) {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "mocked_uploader",
                userId,
                roles,
            )
        val mockSecurityContext = mock(SecurityContext::class.java)
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }
}
