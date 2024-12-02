package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class NonSourceableDataManagerTest(
    @Autowired private val nonSourceableDataRepository: NonSourceableDataRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val objectMapper: ObjectMapper,
) {
    lateinit var nonSourceableDataManager: NonSourceableDataManager
    val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
    private val existingCompanyId = "existingCompanyId"
    private val dataType = DataType("eutaxonomy-financials")
    private val reportingPeriod = "2023"
    private val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)

    @BeforeEach
    fun setup() {
        Mockito.reset(mockDataMetaInformationManager)
        nonSourceableDataManager =
            NonSourceableDataManager(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                objectMapper = objectMapper,
                dataMetaInformationManager = mockDataMetaInformationManager,
                nonSourceableDataRepository = nonSourceableDataRepository,
                companyQueryManager = companyQueryManager,
            )
        nonSourceableDataRepository.deleteAll()

        val nonSourceableEntity =
            NonSourceableEntity(
                eventId = null,
                companyId = existingCompanyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = true,
                reason = "Initial reason",
                creationTime = Instant.now().toEpochMilli(),
                userId = "testUser",
            )
        nonSourceableDataRepository.save(nonSourceableEntity)
    }

    @Test
    fun `check that an exception is thrown when non-existing companyId is provided processing sourcebility storage`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        val dataType = DataType("eutaxonomy-financials")
        val nonSourceableInfo = NonSourceableInfo(nonExistingCompanyId, dataType, "2023", true, "test reason")
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                nonSourceableDataManager.processSourceabilityDataStorageRequest(
                    nonSourceableInfo,
                )
            }
        assertEquals(
            "Dataland does not know the company ID nonExistingCompanyId",
            thrown.message,
        )
    }
}
