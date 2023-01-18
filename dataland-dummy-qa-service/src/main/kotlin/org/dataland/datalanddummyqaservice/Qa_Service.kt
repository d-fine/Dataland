package org.dataland.datalanddummyqaservice

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
@RabbitListener(queues = ["qa_queue"])
class ReceiverAndSender(val rabbitTemplate: RabbitTemplate) {
    @RabbitHandler
    fun receive(message: String) {
        println("Received data upload on QA message queue with Correlation ID:")
        if (message != null) {
            rabbitTemplate.convertAndSend("upload_queue", message)
        }
    }
}
