package org.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.services.QaService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.amqp.core.Message as AMQPMessage

@SpringBootTest(classes = [DatalandQaService::class])
class QaServiceTest {
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val qaService = QaService(mockCloudEventMessageHandler)

    val dataAsString = "TestData"

    @Test
    fun `check an exception is thrown in reading out message from upload queue when dataId is empty`() {
        val thrown = assertThrows<MessageQueueException> {
            qaService.receive(AMQPMessage(dataAsString.toByteArray()))
        }
        val internalMessage = "Error receiving information for QA service. Correlation ID: null"
        Assertions.assertEquals("Error receiving data for QA process: $internalMessage", thrown.message)
    }
}
