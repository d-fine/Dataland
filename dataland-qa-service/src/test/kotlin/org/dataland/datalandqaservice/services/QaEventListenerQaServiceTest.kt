package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewHistoryRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
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

private const val AUTOMATED_QA = "automated-qa-service"

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=nodb"],
)
class QaEventListenerQaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired val testReviewQueueRepository: ReviewQueueRepository,
    @Autowired val testReviewHistoryRepository: ReviewHistoryRepository,
    @Autowired val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaEventListenerQaService: QaEventListenerQaService

    val dataId = "TestDataId"
    val noIdPayload = JSONObject(mapOf("identifier" to "", "comment" to "test")).toString()
    val correlationId = "correlationId"

    private fun getPersistAutomatedQaResultMessage(
        identifier: String,
        validationResult: QaStatus,
        message: String?,
    ): String =
        JSONObject(
            mapOf(
                "identifier" to identifier, "validationResult" to validationResult, "reviewerId" to AUTOMATED_QA,
                "resourceType" to "data", "message" to message,
            ),
        ).toString()

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        qaEventListenerQaService =
            QaEventListenerQaService(
                mockCloudEventMessageHandler,
                objectMapper,
                messageUtils,
                testReviewQueueRepository,
                testReviewHistoryRepository,
                companyDataControllerApi,
                metaDataControllerApi,
            )
    }

    @Test
    fun `check an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.addDataToQueue(noIdPayload, correlationId, MessageType.MANUAL_QA_REQUESTED)
            }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val message =
            objectMapper.writeValueAsString(
                QaCompletedMessage(
                    identifier = dataId,
                    validationResult = QaStatus.Accepted,
                    reviewerId = "someId",
                    message = null,
                ),
            )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QA_COMPLETED, correlationId, ExchangeName.DATA_QUALITY_ASSURED, RoutingKeyNames.DATA,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        val dummyPayload = JSONObject(mapOf("dataId" to dataId, "bypassQa" to true.toString())).toString()
        assertThrows<AmqpException> {
            qaEventListenerQaService.addDataToQueue(dummyPayload, correlationId, MessageType.DATA_STORED)
        }
    }

    @Test
    fun `check an exception is thrown in reading out message from document stored queue when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.assureQualityOfDocument(
                    noIdPayload, correlationId, MessageType.MANUAL_QA_REQUESTED,
                )
            }
        Assertions.assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown in reading out message from data quality assured queue when dataId is empty`() {
        val qaAcceptedNoIdPayload = getPersistAutomatedQaResultMessage("", QaStatus.Accepted, "test message")
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
                    qaAcceptedNoIdPayload,
                    correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
                )
            }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an that the automated qa result is stored correctly in the review history repository`() {
        val acceptedData = "acceptedDataId"
        val automatedQaAcceptedMessage = getPersistAutomatedQaResultMessage(acceptedData, QaStatus.Accepted, "accepted")
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            automatedQaAcceptedMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        testReviewHistoryRepository.findById(acceptedData).ifPresent {
            Assertions.assertEquals(AUTOMATED_QA, it.reviewerKeycloakId)
            Assertions.assertEquals(acceptedData, it.dataId)
            Assertions.assertEquals(QaStatus.Accepted, it.qaStatus)
            Assertions.assertEquals("accepted", it.message)
        }

        val rejectedData = "rejectedDataId"
        val automatedQaRejectedMessage = getPersistAutomatedQaResultMessage(rejectedData, QaStatus.Rejected, "rejected")
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            automatedQaRejectedMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        testReviewHistoryRepository.findById(rejectedData).ifPresent {
            Assertions.assertEquals(AUTOMATED_QA, it.reviewerKeycloakId)
            Assertions.assertEquals(rejectedData, it.dataId)
            Assertions.assertEquals(QaStatus.Rejected, it.qaStatus)
            Assertions.assertEquals("rejected", it.message)
        }
    }

    @Test
    fun `check an that only automated qa results are stored correctly in the review history repository`() {
        val dataId = "thisIdShouldntBeStored"
        val persistAutomatedQaResultMessage =
            JSONObject(
                mapOf(
                    "identifier" to dataId, "validationResult" to QaStatus.Accepted, "reviewerId" to "someReviewerId",
                    "resourceType" to "notData", "message" to "test message",
                ),
            ).toString()
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            persistAutomatedQaResultMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        assertTrue(testReviewHistoryRepository.findById(dataId).isEmpty)
    }
}
