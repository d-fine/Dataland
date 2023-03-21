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

@Component
class DocumentManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun temporarilyStoreDocumentAndTriggerStorage(pdfDocument: MultipartFile): DocumentMetaInfo {
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
        return DocumentMetaInfo(
            documentId = document.bytes.sha256(),
            displayTitle = filename,
            uploaderId = DatalandAuthentication.fromContext().userId,
            uploadTime = Instant.now().toEpochMilli(),
            qaStatus = DocumentQAStatus.Pending,
        )
    }
}