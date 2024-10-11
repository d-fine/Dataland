package org.dataland.documentmanager.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.services.DocumentManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream

/**
 * Controller for the document API
 * @param documentManager the document manager
 */
@RestController
class DocumentController(
    @Autowired private val documentManager: DocumentManager,
) : DocumentApi {
    override fun postDocument(document: MultipartFile): ResponseEntity<DocumentUploadResponse> =
        ResponseEntity.ok(documentManager.temporarilyStoreDocumentAndTriggerStorage(document))

    override fun checkDocument(documentId: String) {
        if (!documentManager.checkIfDocumentExistsWithId(documentId)) {
            throw ResourceNotFoundApiException(
                "Document with ID $documentId does not exist.",
                "Document with ID $documentId does not exist.",
            )
        }
    }

    override fun getDocument(documentId: String): ResponseEntity<InputStreamResource> {
        val document = documentManager.retrieveDocumentById(documentId)
        val documentBytes = document.content.inputStream.use { it.readBytes() }
        val contentLength = documentBytes.size
        val documentContent = InputStreamResource(ByteArrayInputStream(documentBytes))
        return ResponseEntity
            .ok()
            .contentType(document.type.mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=${document.documentId}.${document.type.fileExtension}",
            ).header(HttpHeaders.CONTENT_LENGTH, contentLength.toString())
            .header(HttpHeaders.CONTENT_TYPE, "${document.type.mediaType}")
            .body(documentContent)
    }
}
