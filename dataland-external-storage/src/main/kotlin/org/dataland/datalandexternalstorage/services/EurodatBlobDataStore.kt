package org.dataland.datalandexternalstorage.services
/*
import org.dataland.datalandexternalstorage.entities.BlobItem
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
class EurodatBlobDataStore(
    // @Autowired private val blobItemRepository: BlobItemRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired var temporarilyCachedDocumentClient: StreamingTemporarilyCachedDocumentControllerApi,
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
                    "documentReceivedEurodatDataStore",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.PrivateRequestReceived, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun retrieveBlobFromDocumentManagerAndStoreToDatabase(
        @Payload blobId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.DocumentReceived)
        if (blobId.isEmpty()) {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
        messageUtils.rejectMessageOnException {
            logger.info("Received BlobId $blobId and CorrelationId: $correlationId")
            val resource = temporarilyCachedDocumentClient.getReceivedData(blobId)
            storeBlobToDatabase(blobId, resource.readBytes())
            logger.info(
                "Inserting blob into database with blob ID: $blobId and correlation ID: $correlationId.",
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                blobId, MessageType.DocumentStored, correlationId, ExchangeName.PrivateItemStored,
                RoutingKeyNames.document,
            )
        }
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
        // TODO Call to eurodat
        //  blobItemRepository.save(blobItem)
        return blobItem
    }
}
*/