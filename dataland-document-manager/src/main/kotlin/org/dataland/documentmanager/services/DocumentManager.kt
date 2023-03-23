package org.dataland.documentmanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentExistsResponse
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentQAStatus
import org.dataland.documentmanager.model.DocumentStream
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Implements the generation of document meta info, storage of the meta info temporarily locally
 * @param inMemoryDocumentStore the wrapper for the map of the saved in memory document meta info
 * @param documentMetaInfoRepository the repository for accessing the meta info database
 */
@Component
class DocumentManager(
    @Autowired val documentMetaInfoRepository: DocumentMetaInfoRepository,
    @Autowired private val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired private val storageApi: StreamingStorageControllerApi,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    // @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val pdfVerificationService: PdfVerificationService,
    @Autowired private var objectMapper: ObjectMapper,

) {
    lateinit var messageUtils: MessageQueueUtils
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores the meta information of a document, saves it temporarily locally and notifies that it can be
     * retrieved for other use
     * @param document the multipart file which contains the uploaded document
     * @returns the meta information for the document
     */
    fun temporarilyStoreDocumentAndTriggerStorage(document: MultipartFile): DocumentMetaInfo {
        val correlationId = randomUUID().toString()
        logger.info("Started temporary storage process for document with correlationId: $correlationId")
        val documentMetaInfo = generateDocumentMetaInfo(document, correlationId)
        val documentExists = documentMetaInfoRepository.existsById(documentMetaInfo.documentId)
        if (documentExists) {
            return documentMetaInfo
        }
        val documentBody = document.bytes
        pdfVerificationService.assertThatBlobLooksLikeAPdf(documentBody, correlationId)
        saveMetaInfoToDatabase(documentMetaInfo, correlationId)
        inMemoryDocumentStore.storeDataInMemory(documentMetaInfo.documentId, documentBody)
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            documentMetaInfo.documentId, MessageType.DocumentReceived, correlationId, ExchangeNames.documentReceived,
        )
        return documentMetaInfo
    }

    /**
     * A wrapper for storing document meta information to the database immediately
     *
     * @param documentMetaInfo the document meta information to store
     */
    @Transactional(propagation = Propagation.NEVER)
    fun saveMetaInfoToDatabase(documentMetaInfo: DocumentMetaInfo, correlationId: String) {
        logger.info("Saving meta info of document with correlationId: $correlationId")
        documentMetaInfoRepository.save(DocumentMetaInfoEntity(documentMetaInfo))
    }

    private fun generateDocumentMetaInfo(document: MultipartFile, correlationId: String): DocumentMetaInfo {
        logger.info("Generate document meta info for document with correlationId: $correlationId")
        val filename = document.originalFilename
            ?: throw InvalidInputApiException(
                "Document without filename received",
                "Document without filename received: $correlationId",
            )
        val documentId = document.bytes.sha256()
        logger.info(
            "Generated hash: $documentId for document with correlationId: $correlationId. " +
                "The hash is also the documentId.",
        )
        return DocumentMetaInfo(
            documentId = documentId,
            displayTitle = filename,
            uploaderId = DatalandAuthentication.fromContext().userId,
            uploadTime = Instant.now().toEpochMilli(),
            qaStatus = DocumentQAStatus.Pending,
        )
    }

    /**
     * This method checks whether a document is already stored in the database or not
     * @param documentId the documentId of the document to be checked
     */
    fun checkIfDocumentExistsWithId(documentId: String): DocumentExistsResponse {
        logger.info("Check if document exists with documentId: $documentId")
        val documentExists = documentMetaInfoRepository.existsById(documentId)
        if (documentExists) {
            logger.info("Document with ID: $documentId exists")
        } else {
            logger.info("Document with ID: $documentId does not exist")
        }
        return DocumentExistsResponse(documentExists)
    }

    /**
     * This method retrieves a document from the storage
     * @param documentId the documentId of the document to be retrieved
     */
    fun retrieveDocumentById(documentId: String): DocumentStream {
        val correlationId = randomUUID().toString()
        val metaDataInfoEntity = documentMetaInfoRepository.findById(documentId).orElseThrow {
            ResourceNotFoundApiException(
                "No document found",
                "No document with ID: $documentId could be found. CorrelationId: $correlationId",
            )
        }
        if (metaDataInfoEntity.qaStatus != DocumentQAStatus.Accepted) {
            throw ResourceNotFoundApiException(
                "No accepted document found",
                "A non-quality-assured document with ID: $documentId was found. " +
                    "Only quality-assured documents can be retrieved. CorrelationId: $correlationId",
            )
        }

        val documentDataStream = retrieveDocumentDataStream(documentId, correlationId)
        return DocumentStream(metaDataInfoEntity.displayTitle, documentDataStream)
    }

    private fun retrieveDocumentDataStream(
        documentId: String,
        correlationId: String,
    ) = InputStreamResource(
        inMemoryDocumentStore.retrieveDataFromMemoryStore(documentId)?.let {
            logger.info("Received document $documentId from temporary storage")
            ByteArrayInputStream(it)
        }
            ?: storageApi.getBlobFromInternalStorage(documentId, correlationId)
                .let {
                    logger.info("Received document $documentId from storage service")
                    it
                },
    )

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
                        Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeNames.itemStored, declare = "false"),
                key = [RoutingKeyNames.document],
            ),
        ],
    )
    fun removeStoredDocumentFromTemporaryStore(
        @Payload documentId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils = MessageQueueUtils()
        messageUtils.validateMessageType(type, MessageType.DocumentStored)
        if (documentId.isNotEmpty()) {
            logger.info("Internal Storage sent a message - job done")
            logger.info(
                "Document with documentId $documentId was successfully stored. Correlation ID: $correlationId.",
            )

            messageUtils.rejectMessageOnException {
                inMemoryDocumentStore.deleteFromInMemoryStore(documentId)
            }
        } else {
            throw MessageQueueRejectException("Provided document ID is empty")
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
                        Argument(name = "x-dead-letter-exchange", value = ExchangeNames.deadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeNames.dataQualityAssured, declare = "false"),
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
        messageUtils = MessageQueueUtils()
        messageUtils.validateMessageType(type, MessageType.QACompleted)
        val documentId = objectMapper.readValue(jsonString, QaCompletedMessage::class.java).identifier
        println("AchtungAchtung $documentId")
        if (documentId.isNotEmpty()) {
            messageUtils.rejectMessageOnException {
                val metaInformation: DocumentMetaInfoEntity = documentMetaInfoRepository.findById(documentId).get()
                metaInformation.qaStatus = DocumentQAStatus.Accepted
                logger.info(
                    "Received quality assurance for document upload with DataId: " +
                        "$documentId with Correlation Id: $correlationId",
                )
            }
        } else {
            throw MessageQueueRejectException("Provided document ID is empty")
        }
    }
}
