package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.entities.BlobItem
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
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
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Simple implementation of a data store for blobs using JPA
 * @param blobItemRepository the JPA repository for storing blobs
 */
@Component
class DatabaseBlobDataStore(
    @Autowired private val blobItemRepository: BlobItemRepository,
    @Autowired val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired val temporarilyCachedDocumentClient: StreamingTemporarilyCachedDocumentControllerApi,
    @Autowired val objectMapper: ObjectMapper = jacksonObjectMapper(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves a blob from the document-manager and stores it in the postgres database.
     * Emits a stored message after this has finished
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "documentReceivedDatabaseDataStore",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DOCUMENT_RECEIVED, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun retrieveBlobFromDocumentManagerAndStoreToDatabase(
        @Payload blobId: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DOCUMENT_RECEIVED)
        if (blobId.isEmpty()) {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        MessageQueueUtils.rejectMessageOnException {
            logger.info("Received BlobId $blobId and CorrelationId: $correlationId")
            val resource = temporarilyCachedDocumentClient.getReceivedData(blobId)
            storeBlobToDatabase(blobId, resource.readBytes())
            logger.info(
                "Inserting blob into database with blob ID: $blobId and correlation ID: $correlationId.",
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                blobId, MessageType.DOCUMENT_STORED, correlationId, ExchangeName.ITEM_STORED,
                RoutingKeyNames.DOCUMENT,
            )
            val body =
                objectMapper.writeValueAsString(
                    ManualQaRequestedMessage(
                        resourceId = blobId,
                        bypassQa = null,
                    ),
                )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                body, MessageType.QA_REQUESTED, correlationId, ExchangeName.ITEM_STORED,
                RoutingKeyNames.DOCUMENT_QA,
            )
        }
    }

    /**
     * Stores the provided binary blob to the database and returns the
     * stored database entity. Also ensures that this function is not executed as part of any transaction.
     * This will guarantee that writing is committed after exit of this method.
     * @param blob the blob to store to the database
     * @return the stored database entity
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobToDatabase(
        blobId: String,
        blob: ByteArray,
    ): BlobItem {
        val blobItem = BlobItem(blobId, blob)
        blobItemRepository.save(blobItem)
        return blobItem
    }

    /**
     * Retrieves the blob data from the database
     * @param blobId the hash of the data to be retrieved
     * @return the blob retrieved from the database
     */
    fun selectBlobById(
        blobId: String,
        correlationId: String,
    ): ByteArray =
        blobItemRepository
            .findById(blobId)
            .orElseThrow {
                logger.info("Blob with ID: $blobId could not be found. Correlation ID: $correlationId.")
                ResourceNotFoundApiException(
                    "Dataset not found",
                    "No blob with the ID: $blobId could be found in the data store.",
                )
            }.data
}
