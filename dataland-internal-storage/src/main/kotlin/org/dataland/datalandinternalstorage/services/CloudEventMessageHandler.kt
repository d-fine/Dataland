package org.dataland.datalandinternalstorage.services

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

@Component("CloudEventMessageHandler")
class CloudEventMessageHandler(
    private val rabbitTemplate: RabbitTemplate,
    @Autowired var objectMapper: ObjectMapper,
    var converter: MessagingMessageConverter? = MessagingMessageConverter()
){

    private fun constructCEMessage(input: String, type: String, correlationId: String): MessageMQ {
        val input2 = input.toByteArray()
        val message = CloudEventMessageBuilder
            .withData(input2)
            .setId(correlationId)
            .setType(type)
            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
            .build(CloudEventMessageUtils.AMQP_ATTR_PREFIX)
        return message
    }

    fun buildCEMessageAndSendToQueue(input: String, type: String = "TestType", correlationId: String, queue: String){
        val messageInput = constructCEMessage(input, type, correlationId)
        rabbitTemplate.send(queue, messageInput)
    }
    fun bodyToString(message: MessageMQ) : String{
        return String(message.body)
    }

    private fun convertMessage(message: MessageResult<ByteArray>) :MessageMQ{
        return converter!!.toMessage(message, AMQPMessageProperties())
    }

}


