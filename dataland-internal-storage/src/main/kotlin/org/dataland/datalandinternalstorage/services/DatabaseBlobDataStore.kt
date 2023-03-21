package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.entities.BlobItem
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.openApiClient.api.TemporarilyCachedDocumentControllerApi
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
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired var temporarilyCachedDocumentClient: TemporarilyCachedDocumentControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores the provided binary blob to the database and returns the
     * sha256 hash of the blob. Also ensures that this function is not executed as part of any transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param blob the blob to store to the database
     * @return the sha256 hash of the blob under which is it now accessible in the database
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "documentReceivedDatabaseDataStore",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeNames.documentReceived, declare = "false"),
                key = [RoutingKeyNames.document],
            ),
        ],
    )
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobToDatabase(
        @Payload documentId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ): String {
        messageUtils.validateMessageType(type, MessageType.DataReceived)
        if (documentId.isNotEmpty()) {
            messageUtils.rejectMessageOnException {
                logger.info("Received DocumentId $documentId and CorrelationId: $correlationId")
                // TODO Check why getReceivedData is byte Array
                val blob = temporarilyCachedDocumentClient.getReceivedData(documentId)[0]
                val blobItem = BlobItem(documentId, blob)
                logger.info(
                    "Inserting document into database with documentId: $documentId and correlation id: " +
                        "$correlationId.",
                )
                blobItemRepository.save(blobItem)
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    documentId, MessageType.DocumentStored, correlationId, ExchangeNames.itemStored,
                    RoutingKeyNames.document,
                )
            }
        } else {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        return documentId
    }

    /**
     * Retrieves the blob data from the database
     * @param sha256hash the hash of the data to be retrieved
     * @return the blob retrieved from the database
     */
    fun selectBlobByHash(sha256hash: String, correlationId: String): ByteArray {
        return blobItemRepository.findById(sha256hash).orElseThrow {
            logger.info("Blob with hash: $sha256hash could not be found. Correlation id: $correlationId.")
            ResourceNotFoundApiException(
                "Dataset not found",
                "No blob with the hash: $sha256hash could be found in the data store.",
            )
        }.data
    }
}
