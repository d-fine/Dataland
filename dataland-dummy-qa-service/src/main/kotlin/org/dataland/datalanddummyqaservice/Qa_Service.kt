package org.dataland.datalanddummyqaservice

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service

@SpringBootApplication
class ConsumerApplication

fun main(args: Array<String>) {
    runApplication<ConsumerApplication>(*args)
}

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
/*
@Service
@RabbitListener(queues = ["upload_queue"])
class Receiver {
    @RabbitHandler
    fun receive(message: String) {
        println("Received data ready to be stored on upload queue. Correlation ID:")
    }
}
*/