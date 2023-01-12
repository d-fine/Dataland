package org.dataland.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
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
class RabbitReceiver {
    @RabbitHandler
    fun receive(name: String) {
        println("Received: '$name'")
    }
}
