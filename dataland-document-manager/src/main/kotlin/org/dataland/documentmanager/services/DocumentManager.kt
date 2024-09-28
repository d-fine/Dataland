package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DocumentStream
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.documentmanager.services.conversion.lowercaseExtension
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
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
    @Autowired private val fileProcessor: FileProcessor,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores the meta information of a document, saves it temporarily locally and notifies that it can be
     * retrieved for other use
     * @param document the multipart file which contains the uploaded document
     * @returns the meta information for the document
     */
    fun temporarilyStoreDocumentAndTriggerStorage(document: MultipartFile): DocumentUploadResponse {
        val correlationId = randomUUID().toString()
        logger.info("Started temporary storage process for document with correlation ID: $correlationId")
        val documentType = categorizeDocumentType(document)
        val documentMetaInfo = generateDocumentMetaInfo(document, documentType, correlationId)
        val documentExists = documentMetaInfoRepository.existsById(documentMetaInfo.documentId)
        if (documentExists) {
            return DocumentUploadResponse(documentMetaInfo.documentId)
        }
        val documentBody = fileProcessor.processFile(document, correlationId)
        saveMetaInfoToDatabase(documentMetaInfo, correlationId)
        inMemoryDocumentStore.storeDataInMemory(documentMetaInfo.documentId, documentBody)
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            documentMetaInfo.documentId, MessageType.DOCUMENT_RECEIVED, correlationId, ExchangeName.DOCUMENT_RECEIVED,
        )
        return DocumentUploadResponse(documentMetaInfo.documentId)
    }

    private fun categorizeDocumentType(document: MultipartFile): DocumentType {
        val documentExtension = document.lowercaseExtension()
        return when (documentExtension) {
            "xls" -> DocumentType.Xls
            "xlsx" -> DocumentType.Xlsx
            "ods" -> DocumentType.Ods
            else -> DocumentType.Pdf
        }
    }

    /**
     * A wrapper for storing document meta information to the database immediately
     *
     * @param documentMetaInfo the document meta information to store
     */
    @Transactional(propagation = Propagation.NEVER)
    fun saveMetaInfoToDatabase(
        documentMetaInfo: DocumentMetaInfo,
        correlationId: String,
    ) {
        logger.info("Saving meta info of document with correlation ID: $correlationId")
        documentMetaInfoRepository.save(DocumentMetaInfoEntity(documentMetaInfo))
    }

    private fun generateDocumentMetaInfo(
        document: MultipartFile,
        documentType: DocumentType,
        correlationId: String,
    ): DocumentMetaInfo {
        logger.info("Generate document meta info for document with correlation ID: $correlationId")
        val documentId = document.bytes.sha256()
        logger.info(
            "Generated hash/document ID: $documentId for document with correlation ID: $correlationId. ",
        )
        return DocumentMetaInfo(
            documentId = documentId,
            documentType = documentType,
            uploaderId = DatalandAuthentication.fromContext().userId,
            uploadTime = Instant.now().toEpochMilli(),
            qaStatus = QaStatus.Pending,
        )
    }

    /**
     * This method checks whether a document is already stored in the database or not
     * @param documentId the documentId of the document to be checked
     * @returns true if the document exists, false otherwise
     */
    fun checkIfDocumentExistsWithId(documentId: String): Boolean {
        logger.info("Check if document exists with ID: $documentId")
        val documentExists = documentMetaInfoRepository.existsById(documentId)
        if (documentExists) {
            logger.info("Document with ID: $documentId exists")
        } else {
            logger.info("Document with ID: $documentId does not exist")
        }
        return documentExists
    }

    /**
     * This method retrieves a document from the storage
     * @param documentId the documentId of the document to be retrieved
     */
    fun retrieveDocumentById(documentId: String): DocumentStream {
        val correlationId = randomUUID().toString()
        val metaDataInfoEntity =
            documentMetaInfoRepository.findById(documentId).orElseThrow {
                ResourceNotFoundApiException(
                    "No document found",
                    "No document with ID: $documentId could be found. Correlation ID: $correlationId",
                )
            }
        if (metaDataInfoEntity.qaStatus != QaStatus.Accepted) {
            throw ResourceNotFoundApiException(
                "No accepted document found",
                "A non-quality-assured document with ID: $documentId was found. " +
                    "Only quality-assured documents can be retrieved. Correlation ID: $correlationId",
            )
        }
        val documentDataStream = retrieveDocumentDataStream(documentId, correlationId)
        return DocumentStream(documentId, metaDataInfoEntity.documentType, documentDataStream)
    }

    private fun retrieveDocumentDataStream(
        documentId: String,
        correlationId: String,
    ): InputStreamResource {
        val inMemoryStoredDocument = inMemoryDocumentStore.retrieveDataFromMemoryStore(documentId)
        return if (inMemoryStoredDocument != null) {
            logger.info("Received document $documentId from temporary storage")
            InputStreamResource(ByteArrayInputStream(inMemoryStoredDocument))
        } else {
            logger.info("Received document $documentId from storage service")
            InputStreamResource(storageApi.getBlobFromInternalStorage(documentId, correlationId))
        }
    }
}
