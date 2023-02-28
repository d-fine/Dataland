package org.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.services.QaService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.amqp.core.Message as AMQPMessage

@SpringBootTest(classes = [DatalandQaService::class])
class QaServiceTest {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaService: QaService

    val dataAsString = "TestData"
    val dataId = "TestDataId"

    fun buildDummyMessage(data: String): AMQPMessage {
        return AMQPMessage(data.toByteArray())
    }

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        qaService = QaService(mockCloudEventMessageHandler)
    }

    @Test
    fun `check an exception is thrown in reading out message from upload queue when dataId is empty`() {
        val message = buildDummyMessage(dataAsString)
        val thrown = assertThrows<MessageQueueException> {
            qaService.receive(message)
        }
        val internalMessage = "Error receiving information for QA service. Correlation ID: null"
        Assertions.assertEquals("Error receiving data for QA process: $internalMessage", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val message = buildDummyMessage(dataAsString)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        `when`(mockCloudEventMessageHandler.bodyToString(message)).thenReturn(dataId)
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, MessageType.QACompleted.id, correlationId, "",
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            qaService.receive(message)
        }
    }
}
