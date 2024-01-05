package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewQueueEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
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
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaService(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired val reviewQueueRepository: ReviewQueueRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    data class ForwardedQaMessage(
        val identifier: String,
        val comment: String,
    )

    /**
     * Method to retrieve message from dataStored exchange and constructing new one for qualityAssured exchange
     * @param dataId the data ID
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "manualQaRequestedDataQaService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.ManualQaRequested, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    @Transactional
    fun addDataToQueue(
        @Payload messageAsJsonString: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.ManualQaRequested)
        val message = objectMapper.readValue(messageAsJsonString, ForwardedQaMessage::class.java)
        val dataId = message.identifier
        val comment = message.comment
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        messageUtils.rejectMessageOnException {
            logger.info("Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId")
            storeDatasetAsToBeReviewed(dataId, comment)
        }
    }

    private fun storeDatasetAsToBeReviewed(dataId: String, comment: String) {
        reviewQueueRepository.save(
            ReviewQueueEntity(
                dataId = dataId,
                receptionTime = Instant.now().toEpochMilli(),
                comment = comment,
            ),
        )
    }

    /**
     * Method to retrieve message from dataStored exchange and constructing new one for quality_Assured exchange
     * @param documentId the Hash of the document to be QAed
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "manualQaRequestedDocumentQaService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.ManualQaRequested, declare = "false"),
                key = [RoutingKeyNames.document],
            ),
        ],
    )
    fun assureQualityOfDocument(
        @Payload messageAsJsonString: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.ManualQaRequested)
        val message = objectMapper.readValue(messageAsJsonString, ForwardedQaMessage::class.java)
        val documentId = message.identifier
        if (documentId.isEmpty()) {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        messageUtils.rejectMessageOnException {
            logger.info(
                "Received document with Hash: $documentId on QA message queue with Correlation Id: $correlationId",
            )
            val messageToSend = objectMapper.writeValueAsString(
                QaCompletedMessage(documentId, QaStatus.Accepted),
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                messageToSend, MessageType.QaCompleted, correlationId, ExchangeName.DataQualityAssured,
                RoutingKeyNames.document,
            )
        }
    }
}
