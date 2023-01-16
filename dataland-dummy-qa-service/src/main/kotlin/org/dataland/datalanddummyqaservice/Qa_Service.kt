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
    fun receive(name: String) {
        println("Received entry on QA message queue: '$name'")
        if(name != null) {
            rabbitTemplate.convertAndSend("upload_queue", name)
        }
    }

}
@Service
@RabbitListener(queues = ["upload_queue"])
class Receiver {
    @RabbitHandler
    fun receive(name: String) {
        println("Received data on upload queue, ready to store: '$name'")
    }
}