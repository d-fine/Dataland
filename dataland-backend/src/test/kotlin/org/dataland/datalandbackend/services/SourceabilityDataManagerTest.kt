package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.repositories.SourceabilityDataRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityRejectedEventPayload
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
class SourceabilityDataManagerTest(
    @Autowired private val sourceabilityDataRepository: SourceabilityDataRepository,
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val objectMapper: ObjectMapper,
) {
    lateinit var sourceabilityDataManager: SourceabilityDataManager
    val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
    private val existingCompanyId = "existingCompanyId"
    private val dataType = DataType("eutaxonomy-financials")
    private val reportingPeriod = "2023"
    private val mockCloudEventMessageHandler: CloudEventMessageHandler =
        mock(CloudEventMessageHandler::class.java)

    @BeforeEach
    fun setup() {
        Mockito.reset(mockDataMetaInformationManager)
        sourceabilityDataManager =
            SourceabilityDataManager(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                objectMapper = objectMapper,
                dataMetaInformationManager = mockDataMetaInformationManager,
                sourceabilityDataRepository = sourceabilityDataRepository,
                nonSourceabilityDataRepository = nonSourceabilityDataRepository,
                companyQueryManager = companyQueryManager,
            )
        sourceabilityDataRepository.deleteAll()

        val sourceabilityEntity =
            SourceabilityEntity(
                eventId = null,
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = true,
                reason = "Initial reason",
                creationTime = Instant.now().toEpochMilli(),
                userId = "testUser",
            )
        sourceabilityDataRepository.save(sourceabilityEntity)
    }

    @Test
    fun `check that an exception is thrown when non existing companyId is provided processing sourceability storage`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = nonExistingCompanyId,
                dataType = dataType,
                reportingPeriod = "2023",
                reason = "test reason",
                bypassQa = false,
                isNonSourceable = true,
            )
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
    fun `check that processSourceabilityDataStorageRequest does store an NonSourceableEntity when no dataMetaInfo exists`() {
        val mockCompanyQueryManager = mock(CompanyQueryManager::class.java)
        val expectedSetOfRolesForUploader =
            setOf(DatalandRealmRole.ROLE_UPLOADER)
        mockSecurityContext("testUserId", expectedSetOfRolesForUploader)

        sourceabilityDataManager =
            SourceabilityDataManager(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                objectMapper = objectMapper,
                dataMetaInformationManager = mockDataMetaInformationManager,
                sourceabilityDataRepository = sourceabilityDataRepository,
                nonSourceabilityDataRepository = nonSourceabilityDataRepository,
                companyQueryManager = mockCompanyQueryManager,
            )
        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = "existingCompanyId",
                dataType = dataType,
                reportingPeriod = "2023",
                reason = "Test reason",
                bypassQa = false,
                isNonSourceable = true,
            )
        doNothing()
            .whenever(mockCompanyQueryManager)
            .assertCompanyIdExists(sourceabilityInfo.companyId)
        whenever(
            mockDataMetaInformationManager.searchDataMetaInfo(
                DataMetaInformationSearchFilter(
                    companyId = sourceabilityInfo.companyId,
                    dataType = sourceabilityInfo.dataType,
                    onlyActive = false,
                    reportingPeriod = sourceabilityInfo.reportingPeriod,
                    qaStatus = QaStatus.Accepted,
                ),
            ),
        ).thenReturn(emptyList())
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
        verify(mockCloudEventMessageHandler).buildCEMessageAndSendToQueue(
            any(), any(), any(), any(), any(),
        )
    }

    @Test
    fun `check that a sourceable dataset is stored to the repository`() {
        val reportingPeriod = "2023"
        val uploaderId = "testUploaderId"
        val nonSourceable = false

        sourceabilityDataManager.storeSourceableData(
            companyId = existingCompanyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            uploaderId = uploaderId,
        )

        val nonSourceableData =
            sourceabilityDataManager.getSourceabilityDataByFilters(
                existingCompanyId,
                dataType, reportingPeriod, nonSourceable,
            )
        assertEquals(existingCompanyId, nonSourceableData[0].companyId)
        assertEquals(reportingPeriod, nonSourceableData[0].reportingPeriod)
        assertEquals(dataType, nonSourceableData[0].dataType)
        assertEquals(false, nonSourceableData[0].isNonSourceable)
    }

    @Test
    fun `check that latest sourceability info is returned for known dataset`() {
        val latestInfo =
            sourceabilityDataManager.getLatestSourceabilityInfoForDataset(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
            )

        assertNotNull(latestInfo)
        assertEquals(existingCompanyId, latestInfo!!.companyId)
        assertEquals(dataType, latestInfo.dataType)
        assertEquals(reportingPeriod, latestInfo.reportingPeriod)
        assertEquals(true, latestInfo.isNonSourceable)
    }

    @Test
    fun `check that sourceability info can be filtered by data dimensions and flag`() {
        val filteredInfo =
            sourceabilityDataManager.getSourceabilityDataByFilters(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                nonSourceable = true,
            )

        assertEquals(1, filteredInfo.size)
        assertEquals(existingCompanyId, filteredInfo.first().companyId)
        assertEquals(dataType, filteredInfo.first().dataType)
        assertEquals(reportingPeriod, filteredInfo.first().reportingPeriod)
        assertEquals(true, filteredInfo.first().isNonSourceable)
    }

    @Test
    fun `check replay coverage for non-sourceable storage request`() {
        val mockCompanyQueryManager = mock(CompanyQueryManager::class.java)
        val expectedSetOfRolesForUploader = setOf(DatalandRealmRole.ROLE_UPLOADER)
        mockSecurityContext("testUserId", expectedSetOfRolesForUploader)

        sourceabilityDataManager =
            SourceabilityDataManager(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                objectMapper = objectMapper,
                dataMetaInformationManager = mockDataMetaInformationManager,
                sourceabilityDataRepository = sourceabilityDataRepository,
                nonSourceabilityDataRepository = nonSourceabilityDataRepository,
                companyQueryManager = mockCompanyQueryManager,
            )

        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = "2023",
                reason = "replay test",
                bypassQa = false,
                isNonSourceable = true,
            )

        doNothing().whenever(mockCompanyQueryManager).assertCompanyIdExists(sourceabilityInfo.companyId)
        whenever(
            mockDataMetaInformationManager.searchDataMetaInfo(
                DataMetaInformationSearchFilter(
                    companyId = sourceabilityInfo.companyId,
                    dataType = sourceabilityInfo.dataType,
                    onlyActive = false,
                    reportingPeriod = sourceabilityInfo.reportingPeriod,
                    qaStatus = QaStatus.Accepted,
                ),
            ),
        ).thenReturn(emptyList())

        assertDoesNotThrow {
            sourceabilityDataManager.processSourceabilityDataStorageRequest(sourceabilityInfo)
            sourceabilityDataManager.processSourceabilityDataStorageRequest(sourceabilityInfo)
        }

        val filteredInfo =
            sourceabilityDataManager.getSourceabilityDataByFilters(
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                nonSourceable = true,
            )
        assertNotNull(filteredInfo)
        assertEquals(true, filteredInfo.isNotEmpty())
    }

    @Test
    fun `check that processing accepted qa non-sourceability event activates request`() {
        val now = Instant.now().toEpochMilli()
        val saved =
            nonSourceabilityDataRepository.save(
                NonSourceabilityInformationEntity(
                    nonSourceabilityId = null,
                    companyId = existingCompanyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "pending review",
                    uploaderUserId = "testUser",
                    uploadTime = now,
                    qaStatus = QaNonSourceabilityStatus.Pending,
                    currentlyActive = false,
                    bypassQa = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        val nonSourceabilityId = requireNotNull(saved.nonSourceabilityId)

        sourceabilityDataManager.processQaNonSourceabilityAcceptedEvent(
            QaNonSourceabilityAcceptedEventPayload(
                eventId = UUID.randomUUID(),
                nonSourceabilityId = nonSourceabilityId,
                reviewerUserId = "reviewer-1",
                qaComment = "accepted",
                decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
            ),
            correlationId = "corr-accept-1",
        )

        val updated = nonSourceabilityDataRepository.findById(nonSourceabilityId).orElseThrow()
        assertEquals(QaNonSourceabilityStatus.Accepted, updated.qaStatus)
        assertEquals(true, updated.currentlyActive)
    }

    @Test
    fun `check replay coverage for accepted qa non-sourceability event`() {
        val now = Instant.now().toEpochMilli()
        val saved =
            nonSourceabilityDataRepository.save(
                NonSourceabilityInformationEntity(
                    nonSourceabilityId = null,
                    companyId = existingCompanyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "pending review",
                    uploaderUserId = "testUser",
                    uploadTime = now,
                    qaStatus = QaNonSourceabilityStatus.Pending,
                    currentlyActive = false,
                    bypassQa = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        val nonSourceabilityId = requireNotNull(saved.nonSourceabilityId)

        val payload =
            QaNonSourceabilityAcceptedEventPayload(
                eventId = UUID.randomUUID(),
                nonSourceabilityId = nonSourceabilityId,
                reviewerUserId = "reviewer-1",
                qaComment = "accepted",
                decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
            )

        assertDoesNotThrow {
            sourceabilityDataManager.processQaNonSourceabilityAcceptedEvent(payload, correlationId = "corr-accept-2")
            sourceabilityDataManager.processQaNonSourceabilityAcceptedEvent(payload, correlationId = "corr-accept-2")
        }

        val updated = nonSourceabilityDataRepository.findById(nonSourceabilityId).orElseThrow()
        assertEquals(QaNonSourceabilityStatus.Accepted, updated.qaStatus)
        assertEquals(true, updated.currentlyActive)
    }

    @Test
    fun `check that processing rejected qa non-sourceability event stores rejected and keeps inactive`() {
        val now = Instant.now().toEpochMilli()
        val saved =
            nonSourceabilityDataRepository.save(
                NonSourceabilityInformationEntity(
                    nonSourceabilityId = null,
                    companyId = existingCompanyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "pending review",
                    uploaderUserId = "testUser",
                    uploadTime = now,
                    qaStatus = QaNonSourceabilityStatus.Pending,
                    currentlyActive = false,
                    bypassQa = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        val nonSourceabilityId = requireNotNull(saved.nonSourceabilityId)

        sourceabilityDataManager.processQaNonSourceabilityRejectedEvent(
            QaNonSourceabilityRejectedEventPayload(
                eventId = UUID.randomUUID(),
                nonSourceabilityId = nonSourceabilityId,
                reviewerUserId = "reviewer-1",
                qaComment = "rejected",
                decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
            ),
            correlationId = "corr-reject-1",
        )

        val updated = nonSourceabilityDataRepository.findById(nonSourceabilityId).orElseThrow()
        assertEquals(QaNonSourceabilityStatus.Rejected, updated.qaStatus)
        assertEquals(false, updated.currentlyActive)
    }

    @Test
    fun `check replay coverage for rejected qa non-sourceability event`() {
        val now = Instant.now().toEpochMilli()
        val saved =
            nonSourceabilityDataRepository.save(
                NonSourceabilityInformationEntity(
                    nonSourceabilityId = null,
                    companyId = existingCompanyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "pending review",
                    uploaderUserId = "testUser",
                    uploadTime = now,
                    qaStatus = QaNonSourceabilityStatus.Pending,
                    currentlyActive = false,
                    bypassQa = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        val nonSourceabilityId = requireNotNull(saved.nonSourceabilityId)

        val payload =
            QaNonSourceabilityRejectedEventPayload(
                eventId = UUID.randomUUID(),
                nonSourceabilityId = nonSourceabilityId,
                reviewerUserId = "reviewer-1",
                qaComment = "rejected",
                decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
            )

        assertDoesNotThrow {
            sourceabilityDataManager.processQaNonSourceabilityRejectedEvent(payload, correlationId = "corr-reject-2")
            sourceabilityDataManager.processQaNonSourceabilityRejectedEvent(payload, correlationId = "corr-reject-2")
        }

        val updated = nonSourceabilityDataRepository.findById(nonSourceabilityId).orElseThrow()
        assertEquals(QaNonSourceabilityStatus.Rejected, updated.qaStatus)
        assertEquals(false, updated.currentlyActive)
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
