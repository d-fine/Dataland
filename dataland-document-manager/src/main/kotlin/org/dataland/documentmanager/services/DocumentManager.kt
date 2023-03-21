package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentExistsResponse
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentQAStatus
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Implements the generation of document meta info, storage of the meta info temporarily locally
 * @param inMemoryDocumentStore the wrapper for the map of the saved in memory document meta info
 * @param documentMetaInfoRepository the repository for accessing the meta info database
 */
@Component
class DocumentManager(
    @Autowired val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired val documentMetaInfoRepository: DocumentMetaInfoRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores the meta information of a document, saves it temporarily locally and notifies that it can be
     * retrieved for other use
     *
     * @param document the multipart file which contains the uploaded document
     * @returns the metainformation for the document
     */
    fun temporarilyStoreDocumentAndTriggerStorage(document: MultipartFile): DocumentMetaInfo {
        val correlationId = randomUUID().toString()
        logger.info("Started temporary storage process for document with correlationId: $correlationId")
        val documentMetaInfo = generateDocumentMetaInfo(document, correlationId)
        saveMetaInfoToDatabase(documentMetaInfo)
        inMemoryDocumentStore.storeDataInMemory(documentMetaInfo.documentId, document.bytes)
        // TODO sent message
        return documentMetaInfo
    }

    /**
     * A wrapper for storing document meta information to the database immediately
     *
     * @param documentMetaInfo the document meta information to store
     */
    @Transactional(propagation = Propagation.NEVER)
    fun saveMetaInfoToDatabase(documentMetaInfo: DocumentMetaInfo) {
        documentMetaInfoRepository.save(DocumentMetaInfoEntity(documentMetaInfo))
    }

    private fun generateDocumentMetaInfo(document: MultipartFile, correlationId: String): DocumentMetaInfo {
        logger.info("Generate document meta info for document with correlationId: $correlationId")
        val filename = document.originalFilename ?:
            throw InvalidInputApiException(
            "Document without filename received",
            "Document without filename received: $correlationId",
            )
        val documentId = document.bytes.sha256()
        logger.info("Generated hash: $documentId for document with correlationId: $correlationId. " +
                "The hash is also the documentId.")
        return DocumentMetaInfo(
            documentId = documentId,
            displayTitle = filename,
            uploaderId = DatalandAuthentication.fromContext().userId,
            uploadTime = Instant.now().toEpochMilli(),
            qaStatus = DocumentQAStatus.Pending,
        )
    }

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
}