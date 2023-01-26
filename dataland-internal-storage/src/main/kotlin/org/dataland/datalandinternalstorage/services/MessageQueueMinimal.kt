package org.dataland.datalandinternalstorage.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.ComponentScan
import org.springframework.amqp.core.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.converter.MessageConverter


/**
 * Simple implementation of a data store using a postgres database
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component

class MessageQueueMinimal(
) {
    @RabbitListener(queues = ["storage_queue"])
    fun Function(@Payload message: Message, @Header header :MessageHeaders){
        val content_type = header.getValue("CONTENT_TYPE")
        //mc = MessageConverter().fromMessage(message, class(content_type))
        println("Retrieved Message:${content_type}")
    }
}
