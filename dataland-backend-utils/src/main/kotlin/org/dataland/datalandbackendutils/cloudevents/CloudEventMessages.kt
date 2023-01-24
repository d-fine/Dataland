package org.dataland.datalandbackendutils.cloudevents

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.function.cloudevent.CloudEventMessageUtils
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import org.springframework.amqp.rabbit.core.RabbitTemplate

/**
 * This class ensures that the errorResponse is mapped as the default response
 * for non-explicitly set response codes as suggested in the swagger-docs
 * (ref https://swagger.io/docs/specification/describing-responses/)
 */

@Component("CloudEventMessages")
class CloudEventMessages(
    @Autowired var cloudEventBuilder: CloudEventMessages,
    private val rabbitTemplate: RabbitTemplate
){


    private fun buildRQMessage(input: String, type: String) :Message<String>{
        var message = CloudEventMessageBuilder
            .withData(input)
            .setType(type)
            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
            .build(CloudEventMessageUtils.AMQP_ATTR_PREFIX);
        return message
    }

    fun buildAndSendMessage(input: String, type: String = "TestType", queue: String){
        var messageInput = cloudEventBuilder.buildRQMessage(input, type)
        rabbitTemplate.convertAndSend(queue, messageInput)
    }

}


