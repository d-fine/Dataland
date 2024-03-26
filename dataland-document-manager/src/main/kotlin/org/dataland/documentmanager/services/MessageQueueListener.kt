package org.dataland.documentmanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
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

/**
 * Implements the generation of document meta info, storage of the meta info temporarily locally
 * @param inMemoryDocumentStore the wrapper for the map of the saved in memory document meta info
 * @param documentMetaInfoRepository the repository for accessing the meta info database
 */
@Component
class MessageQueueListener(
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired val documentMetaInfoRepository: DocumentMetaInfoRepository,
    @Autowired private val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired private var objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the stored queue and removes data entries from the temporary storage once they have been
     * stored in the persisted database. Further it logs success notification associated containing documentId and
     * correlationId
     * @param documentId the ID of the dataset to that was stored
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataStoredDocumentManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.ItemStored, declare = "false"),
                key = [RoutingKeyNames.document],
            ),
        ],
    )
    fun removeStoredDocumentFromTemporaryStore(
        @Payload documentId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.DocumentStored)
        if (documentId.isEmpty()) {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        logger.info("Internal Storage sent a message - job done")
        logger.info(
            "Document with ID $documentId was successfully stored. Correlation ID: $correlationId.",
        )
        messageUtils.rejectMessageOnException {
            inMemoryDocumentStore.deleteFromInMemoryStore(documentId)
        }
    }

    /**
     * Method that listens to the qa_queue and updates the metadata information after successful qa process
     * @param jsonString the message describing the result of the completed QA process
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "documentQualityAssuredDocumentManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.DataQualityAssured, declare = "false"),
                key = [RoutingKeyNames.document],
            ),
        ],
    )
    @Transactional
    fun updateDocumentMetaData(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.QaCompleted)
        val documentId = objectMapper.readValue(jsonString, QaCompletedMessage::class.java).identifier
        if (documentId.isEmpty()) {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        messageUtils.rejectMessageOnException {
            val metaInformation: DocumentMetaInfoEntity = documentMetaInfoRepository.findById(documentId).get()
            metaInformation.qaStatus = QaStatus.Accepted
            logger.info(
                "Received quality assurance for document upload with document ID: " +
                    "$documentId with Correlation ID: $correlationId",
            )
        }
    }
}
