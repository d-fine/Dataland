package org.dataland.datalandinternalstorage.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.ComponentScan
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.converter.MessageConverter


/**
 * Simple implementation of a data store using a postgres database
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component

class CloudEventsConverter : MessageConverter {
    override fun fromMessage(message: Message<*>, targetClass: Class<*>): Any? {
        TODO("Not yet implemented")
    }

    override fun toMessage(payload: Any, headers: MessageHeaders?): Message<*>? {
        TODO("Not yet implemented")
    }
}
