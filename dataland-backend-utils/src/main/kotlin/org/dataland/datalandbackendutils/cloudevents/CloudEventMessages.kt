package org.dataland.datalandbackendutils.cloudevents

import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.function.cloudevent.CloudEventMessageUtils
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils

/**
 * This class ensures that the errorResponse is mapped as the default response
 * for non-explicitly set response codes as suggested in the swagger-docs
 * (ref https://swagger.io/docs/specification/describing-responses/)
 */

@Component("CloudEventMessages")
class CloudEventMessages{
    fun buildRQMessage(input :String) :Message<String>{
        var message = CloudEventMessageBuilder
            .withData(input)
            .setType("TestType")
            .setSubject("TestSubject")
            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
            .build(CloudEventMessageUtils.AMQP_ATTR_PREFIX);
        return message
    }
}


