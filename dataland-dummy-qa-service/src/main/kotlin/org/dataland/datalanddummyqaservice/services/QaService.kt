package org.dataland.datalanddummyqaservice

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.dataland.datalandbackendutils.cloudevents.CloudEventMessageHandler
import org.springframework.amqp.core.Message
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

/**
 * This class holds the function to start the dummy QA service
 */
@SpringBootApplication
class ConsumerApplication
/**
 * This class holds the function to run the dummy QA service
 */
fun main(args: Array<String>) {
    runApplication<ConsumerApplication>(*args)
}

/**
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param rabbitTemplate
 */
@Component
@ComponentScan("org.dataland")
class QaService(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to retrieve message from upload_queue and constructing new one for qa_queue
     * @param message Message retrieved from upload_queue
     */
    @RabbitListener(queues = ["upload_queue"])
    fun receive(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (dataId.isNotEmpty()){
            logger.info("Received data upload with DataId: $dataId on QA message queue with Correlation Id: " +
                    "$correlationId")
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "QA Process Completed", correlationId,
                "qa_queue")
        }
    }
}
