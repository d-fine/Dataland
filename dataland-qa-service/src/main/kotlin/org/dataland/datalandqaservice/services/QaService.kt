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
     * Method to retrieve message from dataStored exchange and constructing new one for qualityAssured exchange
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
                    message, MessageType.QACompleted, correlationId, ExchangeNames.dataQualityAssured, "data",
                )
            }
        } else {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
    }

    /**
     * Method to retrieve message from dataStored exchange and constructing new one for quality_Assured exchange
     * @param documentHash the Hash of the document to be QAed
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "documentStoredQaService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeNames.documentStored, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun assureQualityOfDocument(
        @Payload documentHash: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.DocumentStored)
        if (documentHash.isNotEmpty()) {
            messageUtils.rejectMessageOnException {
                logger.info(
                    "Received document with Hash: $documentHash on QA message queue with Correlation Id: $correlationId",
                )
                val message = objectMapper.writeValueAsString(
                    QaCompletedMessage(documentHash, "By default, QA is passed"),
                )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    message, MessageType.QACompleted, correlationId, ExchangeNames.dataQualityAssured, "document",
                )
            }
        } else {
            throw MessageQueueRejectException("Provided document Hash is empty")
        }
    }
}
