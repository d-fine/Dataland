package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.repositories.SourceabilityDataRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SourceabilityDataManagerTest(
    @Autowired private val sourceabilityDataRepository: SourceabilityDataRepository,
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
                companyQueryManager = mockCompanyQueryManager,
            )
        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = "existingCompanyId",
                dataType = dataType,
                reportingPeriod = "2023",
                isNonSourceable = true,
                reason = "Test reason",
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
