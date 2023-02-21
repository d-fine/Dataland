package org.dataland.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
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
     * @param message Message retrieved from upload_queue
     */
    @RabbitListener(queues = ["upload_queue"])
    fun receive(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (!dataId.isNullOrEmpty()) {
            logger.info(
                "Received data upload with DataId: $dataId on QA message queue with Correlation Id: " +
                    correlationId,
            )
            try {
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    dataId, "QA Process Completed", correlationId,
                    "qa_queue",
                )
            } catch (exception: AmqpException) {
                val internalMessage = "Error sending message to qa_queue." +
                    " Received AmqpException with message: ${exception.message}. Correlation ID: $correlationId."
                logger.error(internalMessage)
                throw AmqpException(internalMessage, exception)
            }
        } else {
            val internalMessage = "Error receiving information for QA service. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw MessageQueueException(
                "Error receiving data for QA process: $internalMessage",
            )
        }
    }
}
