package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.AssembledDataMigrationManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=nodb"],
)
class QaEventListenerQaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val testQaReviewRepository: QaReviewRepository,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaEventListenerQaService: QaEventListenerQaService
    lateinit var mockQaReviewManager: QaReviewManager
    lateinit var mockDataPointQaReviewManager: DataPointQaReviewManager
    lateinit var mockQaReportManager: QaReportManager
    lateinit var mockMetaDataControllerApi: MetaDataControllerApi
    lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    lateinit var mockDataPointControllerApi: DataPointControllerApi
    lateinit var mockAssembledDataMigrationManager: AssembledDataMigrationManager

    val dataId = "TestDataId"
    val correlationId = "correlationId"

    private fun getQaMessagePayload(
        dataId: String,
        bypassQa: Boolean,
    ): String =
        objectMapper.writeValueAsString(
            DataUploadedPayload(
                dataId = dataId,
                bypassQa = bypassQa,
            ),
        )

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)

        mockMetaDataControllerApi = mock(MetaDataControllerApi::class.java)
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        mockQaReviewManager = mock(QaReviewManager::class.java)
        mockDataPointQaReviewManager = mock(DataPointQaReviewManager::class.java)
        mockQaReportManager = mock(QaReportManager::class.java)
        mockDataPointControllerApi = mock(DataPointControllerApi::class.java)
        mockAssembledDataMigrationManager = mock(AssembledDataMigrationManager::class.java)
        qaEventListenerQaService =
            QaEventListenerQaService(
                mockCloudEventMessageHandler,
                objectMapper,
                mockQaReviewManager,
                mockDataPointQaReviewManager,
                mockQaReportManager,
                mockMetaDataControllerApi,
                mockDataPointControllerApi,
                mockAssembledDataMigrationManager,
            )
    }

    @Test
    fun `check that an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val noIdPayload = getQaMessagePayload("", false)
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService
                    .addDatasetToQaReviewRepository(noIdPayload, correlationId, MessageType.PUBLIC_DATA_RECEIVED)
            }
        Assertions.assertEquals("Invalid UUID string: ", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val message =
            objectMapper.writeValueAsString(
                ManualQaRequestedMessage(
                    resourceId = dataId,
                    bypassQa = false,
                ),
            )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QA_STATUS_UPDATED, correlationId, ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, RoutingKeyNames.DATA,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            qaEventListenerQaService.addDatasetToQaReviewRepository(message, correlationId, MessageType.DATA_STORED)
        }
    }

    @Test
    fun `check that an exception is thrown in reading out message from document stored queue when documentId is empty`() {
        val noIdPayload = objectMapper.writeValueAsString(ManualQaRequestedMessage("", false))
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.assureQualityOfDocument(
                    noIdPayload, correlationId, MessageType.QA_REQUESTED,
                )
            }
        Assertions.assertEquals("Message was rejected: Provided document ID is empty (correlationId: $correlationId)", thrown.message)
    }

    @Test
    fun `check that a bypassQA result is stored correctly in the QA review repository`() {
        val dataId = UUID.randomUUID().toString()
        val manualQaRequestedMessage = getQaMessagePayload(dataId = dataId, bypassQa = true)

        val acceptedStoredCompanyJson = "json/services/StoredCompanyAccepted.json"
        val acceptedStoredCompany = objectMapper.readValue(getJsonString(acceptedStoredCompanyJson), StoredCompany::class.java)
        val acceptedDataMetaInformation: DataMetaInformation = acceptedStoredCompany.dataRegisteredByDataland[0]
        val acceptedDataId = acceptedDataMetaInformation.dataId

        `when`(mockMetaDataControllerApi.getDataMetaInfo(dataId)).thenReturn(acceptedDataMetaInformation)
        `when`(mockCompanyDataControllerApi.getCompanyById(acceptedDataMetaInformation.companyId)).thenReturn(acceptedStoredCompany)

        qaEventListenerQaService.addDatasetToQaReviewRepository(
            manualQaRequestedMessage, correlationId, MessageType.PUBLIC_DATA_RECEIVED,
        )

        testQaReviewRepository.findFirstByDataIdOrderByTimestampDesc(acceptedDataId)?.let {
            Assertions.assertEquals("", it.triggeringUserId)
            Assertions.assertEquals(acceptedDataId, it.dataId)
            Assertions.assertEquals(QaStatus.Accepted, it.qaStatus)
            Assertions.assertEquals("Automatically QA approved", it.comment)
        }
    }

    private fun getJsonString(resourceFile: String): String =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            ).toString()
}
