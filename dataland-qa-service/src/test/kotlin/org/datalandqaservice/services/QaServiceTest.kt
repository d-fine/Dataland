package org.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.services.QaService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandQaService::class])
class QaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaService: QaService

    val dataId = "TestDataId"

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        qaService = QaService(mockCloudEventMessageHandler, objectMapper)
    }

    @Test
    fun `check an exception is thrown in reading out message from upload queue when dataId is empty`() {
        val correlationId = "correlationId"
        val thrown = assertThrows<MessageQueueException> {
            qaService.assureQualityOfData("", correlationId, MessageType.DataStored)
        }
        val internalMessage = "Error receiving information for QA service. Correlation ID: $correlationId"
        Assertions.assertEquals("Error receiving data for QA process: $internalMessage", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val correlationId = "correlationId"
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(
                dataId = dataId,
                validationResult = "By default, QA is passed",
            ),
        )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QACompleted, correlationId, ExchangeNames.dataQualityAssured,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            qaService.assureQualityOfData(dataId, correlationId, MessageType.DataStored)
        }
    }
}
