package org.dataland.datalandbackendutils.cloudevents

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessagingMessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder
import org.springframework.cloud.function.cloudevent.CloudEventMessageUtils
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import org.springframework.amqp.core.Message as MessageMQ
import org.springframework.messaging.Message as MessageResult
import org.springframework.amqp.core.MessageProperties as AMQPMessageProperties


/**
 * This class ensures that the errorResponse is mapped as the default response
 * for non-explicitly set response codes as suggested in the swagger-docs
 * (ref https://swagger.io/docs/specification/describing-responses/)
 */

/**
 * Handling of messages in the CloudEvents format
 * @param rabbitTemplate
 * @param objectMapper
 * @param converter service for converting CloudEvents message to object treatable by RabbitMQ
 */

@Component("CloudEventMessageHandler")
class CloudEventMessageHandler(
    private val rabbitTemplate: RabbitTemplate,
    @Autowired var objectMapper: ObjectMapper,
){
    var converter: MessagingMessageConverter? = MessagingMessageConverter()
    private fun constructCEMessage(input: String, type: String, correlationId: String): MessageMQ {
        val input2 = input.toByteArray()
        val message = CloudEventMessageBuilder
            .withData(input2)
            .setId(correlationId)
            .setType(type)
            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
            .build(CloudEventMessageUtils.AMQP_ATTR_PREFIX)
        return convertMessage(message)
    }

    /**
     * Method constructing a CloudEvents message and sending it to a RabbitMQ message queue
     * @param input the payload of the message to be constructed
     * @param type criterion to distinguish different messages to RabbitMQ apart from used queue
     * @param correlationId to be used as ID in header of CloudEvents message
     * @param queue RabbitMQ message queue to send the constructed message to
     */
    fun buildCEMessageAndSendToQueue(input: String, type: String = "TestType", correlationId: String, queue: String){
        val messageInput = constructCEMessage(input, type, correlationId)
        rabbitTemplate.send(queue, messageInput)
    }

    /**
     * Method to extract the byte payload of a RabbitMQ message as string
     * @param message RabbitMQ message whose payload is to be extracted
     */
    fun bodyToString(message: MessageMQ) : String{
        return String(message.body)
    }

    fun convertMessage(message: MessageResult<ByteArray>) :MessageMQ{
        return converter!!.toMessage(message, AMQPMessageProperties())
    }

}


