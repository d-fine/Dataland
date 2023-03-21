package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID.randomUUID

@Component
class DocumentManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun temporarilyStoreDocumentAndTriggerStorage(pdfDocument: MultipartFile): DocumentMetaInfo {
        val correlationId = randomUUID().toString()
        logger.info("")
        val documentMetaInfo = generateDocumentMetaInfo(pdfDocument, correlationId)
        return documentMetaInfo
    }

    private fun generateDocumentMetaInfo(document: MultipartFile, correlationId: String): DocumentMetaInfo {
        val filename = document.originalFilename ?:
            throw InvalidInputApiException(
            "Document without filename received",
            "Document without filename received: $correlationId",
            )
        //val uploaderId =
        return DocumentMetaInfo(
            documentId = "",
            displayTitle = filename,
            uploaderId = "",
        )
    }
}