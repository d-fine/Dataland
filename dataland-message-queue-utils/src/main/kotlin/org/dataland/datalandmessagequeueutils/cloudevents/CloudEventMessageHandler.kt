package org.dataland.datalandmessagequeueutils.cloudevents

import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessagingMessageConverter
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder
import org.springframework.cloud.function.cloudevent.CloudEventMessageUtils
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import org.springframework.amqp.core.Message as MessageMQ
import org.springframework.amqp.core.MessageProperties as AMQPMessageProperties
import org.springframework.messaging.Message as MessageResult

/**
 * Handling of messages in the CloudEvents format
 * @param rabbitTemplate
 */

@Component("CloudEventMessageHandler")
class CloudEventMessageHandler(
    private val rabbitTemplate: RabbitTemplate,
) {
    var converter: MessagingMessageConverter = MessagingMessageConverter()
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun buildCEMessage(body: String, type: String, correlationId: String): MessageMQ {
        val bodyInBytes = body.toByteArray()
        val message = CloudEventMessageBuilder
            .withData(bodyInBytes)
            .setId(correlationId)
            .setType(type)
            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
            .build(CloudEventMessageUtils.AMQP_ATTR_PREFIX)
        return convertMessage(message)
    }

    /**
     * Method constructing a CloudEvents message and sending it to a RabbitMQ message queue
     * @param body the payload of the message to be constructed
     * @param type criterion to distinguish different messages to RabbitMQ apart from used queue
     * @param correlationId to be used as ID in header of CloudEvents message
     * @param exchange RabbitMQ exchange to send the constructed message to
     */
    fun buildCEMessageAndSendToQueue(
        body: String,
        type: String,
        correlationId: String,
        exchange: String,
        routingKey: String = "",
    ) {
        val messageInput = buildCEMessage(body, type, correlationId)
        try {
            rabbitTemplate.send(exchange, routingKey, messageInput)
        } catch (exception: AmqpException) {
            val internalMessage = "Error sending message to $exchange." +
                " Received AmqpException with message: ${exception.message}. Correlation ID: $correlationId."
            logger.error(internalMessage)
            throw AmqpException(internalMessage, exception)
        }
    }

    private fun convertMessage(message: MessageResult<ByteArray>): MessageMQ {
        return converter.toMessage(message, AMQPMessageProperties())
    }
}
