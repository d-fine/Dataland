package org.dataland.datalanddummyqaservice

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.dataland.datalandbackendutils.cloudevents.CloudEventMessageHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

@SpringBootApplication
class ConsumerApplication

fun main(args: Array<String>) {
    runApplication<ConsumerApplication>(*args)
}
@Component
@ComponentScan(basePackages = ["org.dataland"])
@RabbitListener(queues = ["upload_queue"])
class QaService(val rabbitTemplate: RabbitTemplate,
                @Autowired var cloudEventBuilder: CloudEventMessageHandler) {
    @RabbitHandler
    fun receive(message: String?) {
        println("Received data upload on QA message queue with Correlation ID:")
        println(message)
        if (!message.isNullOrEmpty()){
            cloudEventBuilder.buildCEMessageAndSendToQueue(input = message, type = "CorrelationId on QA", queue = "qa_queue")
        }
    }
}
