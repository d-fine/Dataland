package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaService(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to retrieve message from upload_queue and constructing new one for qa_queue
     * @param dataId the ID of the dataset to be QAed
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue("dataStoredQaService", arguments = [
                    Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                    Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                    Argument(name = "defaultRequeueRejected", value = "false")
                ]),
                exchange = Exchange(ExchangeNames.dataStored, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun assureQualityOfData(
        @Payload dataId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        if (type != MessageType.DataStored) {
            throw AmqpRejectAndDontRequeueException("Message could not be processed - Message rejected");
        }
        if (dataId.isNotEmpty()) {
            logger.info(
                "Received data upload with DataId: $dataId on QA message queue with Correlation Id: $correlationId",
            )
            val message = objectMapper.writeValueAsString(
                QaCompletedMessage(
                    dataId = dataId,
                    validationResult = "By default, QA is passed",
                ),
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QACompleted, correlationId,
                ExchangeNames.dataQualityAssured,
            )
        } else {
            throw AmqpRejectAndDontRequeueException("Message could not be processed - Message rejected");
        }
    }
}
