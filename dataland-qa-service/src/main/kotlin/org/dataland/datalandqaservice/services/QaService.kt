package org.dataland.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderType
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaService(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to retrieve message from upload_queue and constructing new one for qa_queue
     * @param dataId the ID of the dataset to be QAed
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue("dataStoredQaService"),
                exchange = Exchange(ExchangeNames.dataStored, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun assureQualityOfData(
        @Payload dataId: String,
        @Header(MessageHeaderType.CorrelationId) correlationId: String,
        @Header(MessageHeaderType.Type) type: String,
    ) {
        if (type != MessageType.DataStored.name) {
            return
        }
        if (dataId.isNotEmpty()) {
            logger.info(
                "Received data upload with DataId: $dataId on QA message queue with Correlation Id: $correlationId",
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, MessageType.QACompleted.name, correlationId,
                ExchangeNames.dataQualityAssured,
            )
        } else {
            val internalMessage = "Error receiving information for QA service. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw MessageQueueException(
                "Error receiving data for QA process: $internalMessage",
            )
        }
    }
}
