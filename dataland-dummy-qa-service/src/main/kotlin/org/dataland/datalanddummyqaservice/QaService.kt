package org.dataland.datalanddummyqaservice

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.dataland.datalandinternalstorage.services.CloudEventMessageHandler
import org.springframework.amqp.core.Message
import org.slf4j.LoggerFactory
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
class QaService(@Autowired var cloudEventBuilder: CloudEventMessageHandler) {
    private val logger = LoggerFactory.getLogger(javaClass)
    @RabbitHandler
    fun receive(message: Message) {
        val dataId = cloudEventBuilder.bodyToString(message)
        val correlationId = message.messageProperties.messageId
        logger.info("Received data upload with DataId: $dataId on qa message queue with Correlation Id: $correlationId")
        if (!dataId.isNullOrEmpty()){
            cloudEventBuilder.buildCEMessageAndSendToQueue(dataId, "QA Process Completed", correlationId,"qa_queue")
        }
    }
}
