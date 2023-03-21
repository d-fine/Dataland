package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentQAStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
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
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun temporarilyStoreDocumentAndTriggerStorage(document: MultipartFile): DocumentMetaInfo {
        val correlationId = randomUUID().toString()
        logger.info("Started temporary storage process for document with correlationId: $correlationId")
        val documentMetaInfo = generateDocumentMetaInfo(document, correlationId)
        inMemoryDocumentStore.storeDataInMemory(documentMetaInfo.documentId, document.bytes)
        return documentMetaInfo
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
}