package org.dataland.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaService(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Value("\${spring.rabbitmq.qa-queue:}") private val qaQueue: String,
) {companion object {
    private const val uploadQueue = ("\${spring.rabbitmq.upload-queue}")
}
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to retrieve message from upload_queue and constructing new one for qa_queue
     * @param message Message retrieved from upload_queue
     */
    @RabbitListener(queues = [uploadQueue])
    fun receive(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (!dataId.isNullOrEmpty()) {
            logger.info(
                "Received data upload with DataId: $dataId on QA message queue with Correlation Id: $correlationId",
            )
            sendMessageToQueue(dataId, "QA Process Completed", correlationId, qaQueue)
        } else {
            val internalMessage = "Error receiving information for QA service. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw MessageQueueException(
                "Error receiving data for QA process: $internalMessage",
            )
        }
    }

    private fun sendMessageToQueue(dataId: String, type: String, correlationId: String, messageQueue: String) {
        try {
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, type, correlationId, messageQueue,
            )
        } catch (exception: AmqpException) {
            val internalMessage = "Error sending message to $messageQueue." +
                " Received AmqpException with message: ${exception.message}. Correlation ID: $correlationId."
            logger.error(internalMessage)
            throw AmqpException(internalMessage, exception)
        }
    }
}
