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
     * Retrieves a blob from the document-manager and stores it in the postgres database.
     * Emits a stored message after this has finshed
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
    fun retrieveBlobFromDocumentManagerAndStoreToDatabase(
        @Payload blobId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ): String {
        messageUtils.validateMessageType(type, MessageType.DataReceived)
        if (blobId.isNotEmpty()) {
            messageUtils.rejectMessageOnException {
                logger.info("Received BlobId $blobId and CorrelationId: $correlationId")
                val blob = temporarilyCachedDocumentClient.getReceivedData(blobId).readBytes()
                storeBlobToDatabase(blobId, blob)
                logger.info(
                    "Inserting blob into database with BlobId: $blobId and correlation id: " +
                        "$correlationId.",
                )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    blobId, MessageType.DocumentStored, correlationId, ExchangeNames.itemStored,
                    RoutingKeyNames.document,
                )
            }
        } else {
            throw MessageQueueRejectException("Provided BlobId is empty")
        }
        return blobId
    }

    /**
     * Stores the provided binary blob to the database and returns the
     * stored database entity. Also ensures that this function is not executed as part of any transaction.
     * This will guarantee that the write is committed after exit of this method.
     * @param blob the blob to store to the database
     * @return the stored database entity
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobToDatabase(blobId: String, blob: ByteArray): BlobItem {
        val blobItem = BlobItem(blobId, blob)
        blobItemRepository.save(blobItem)
        return blobItem
    }

    /**
     * Retrieves the blob data from the database
     * @param blobId the hash of the data to be retrieved
     * @return the blob retrieved from the database
     */
    fun selectBlobById(blobId: String, correlationId: String): ByteArray {
        return blobItemRepository.findById(blobId).orElseThrow {
            logger.info("Blob with id: $blobId could not be found. Correlation id: $correlationId.")
            ResourceNotFoundApiException(
                "Dataset not found",
                "No blob with the id: $blobId could be found in the data store.",
            )
        }.data
    }
}
