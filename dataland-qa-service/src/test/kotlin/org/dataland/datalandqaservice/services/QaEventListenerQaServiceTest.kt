package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
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

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(classes = [DatalandQaService::class])
class QaEventListenerQaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired val testReviewQueueRepository: ReviewQueueRepository,
    @Autowired val testReviewHistoryRepository: ReviewHistoryRepository,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaEventListenerQaService: QaEventListenerQaService

    val dataId = "TestDataId"
    val noIdPayload = JSONObject(mapOf("identifier" to "", "comment" to "test")).toString()

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        qaEventListenerQaService = QaEventListenerQaService(
            mockCloudEventMessageHandler,
            objectMapper,
            messageUtils,
            testReviewQueueRepository,
            testReviewHistoryRepository,
        )
    }

    @Test
    fun `check an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val correlationId = "correlationId"
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            qaEventListenerQaService.addDataToQueue(noIdPayload, correlationId, MessageType.ManualQaRequested)
        }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val correlationId = "correlationId"
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(
                identifier = dataId,
                validationResult = QaStatus.Accepted,
                reviewerId = "someId",
                message = null,
            ),
        )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QaCompleted, correlationId, ExchangeName.DataQualityAssured, RoutingKeyNames.data,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        val dummyPayload = JSONObject(mapOf("dataId" to dataId, "bypassQa" to true.toString())).toString()
        assertThrows<AmqpException> {
            qaEventListenerQaService.addDataToQueue(dummyPayload, correlationId, MessageType.DataStored)
        }
    }

    @Test
    fun `check an exception is thrown in reading out message from document stored queue when dataId is empty`() {
        val correlationId = "correlationId"
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            qaEventListenerQaService.assureQualityOfDocument(
                noIdPayload, correlationId, MessageType.ManualQaRequested,
            )
        }
        Assertions.assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown in reading out message from data quality assured queue when dataId is empty`() {
        val correlationId = "correlationId"
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            qaEventListenerQaService.addDataToReviewHistory(noIdPayload, correlationId, MessageType.ManualQaRequested)
        }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }
}
