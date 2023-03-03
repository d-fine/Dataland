package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
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
    @Autowired var messageUtils: MessageQueueUtils,
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
                value = Queue(
                    "dataStoredQaService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
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
        messageUtils.validateMessageType(type, MessageType.DataStored)
        if (dataId.isNotEmpty()) {
            messageUtils.rejectMessageOnException {
                logger.info(
                    "Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId",
                )
                val message = objectMapper.writeValueAsString(
                    QaCompletedMessage(dataId, "By default, QA is passed"),
                )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    message, MessageType.QACompleted, correlationId, ExchangeNames.dataQualityAssured,
                )
            }
        } else {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
    }
}
