package org.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.dataland.datalandqaservice.services.QaService
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
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["org.dataland"])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(classes = [DatalandQaService::class])
class QaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired val testReviewQueueRepository: ReviewQueueRepository,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaService: QaService

    val dataId = "TestDataId"

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        qaService = QaService(
            mockCloudEventMessageHandler,
            objectMapper,
            messageUtils,
            testReviewQueueRepository,
        )
    }

    @Test
    fun `check an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val correlationId = "correlationId"
        val dummyPayload = JSONObject(mapOf("dataId" to "", "bypassQa" to true.toString())).toString()
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            qaService.addDataToQueue(dummyPayload, correlationId, MessageType.DataStored)
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
            ),
        )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QaCompleted, correlationId, ExchangeNames.dataQualityAssured, RoutingKeyNames.data,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        val dummyPayload = JSONObject(mapOf("dataId" to dataId, "bypassQa" to true.toString())).toString()
        assertThrows<AmqpException> {
            qaService.addDataToQueue(dummyPayload, correlationId, MessageType.DataStored)
        }
    }

    @Test
    fun `check an exception is thrown in reading out message from document stored queue when dataId is empty`() {
        val correlationId = "correlationId"
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            qaService.assureQualityOfDocument("", correlationId, MessageType.DocumentStored)
        }
        Assertions.assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }
}
