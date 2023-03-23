package org.dataland.documentmanager.controller

import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.model.DocumentExistsResponse
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.services.DocumentManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Controller for the document API
 * @param documentManager the document manager
 */
@RestController
class DocumentController(
    @Autowired private val documentManager: DocumentManager,
) : DocumentApi {
    override fun postDocument(pdfDocument: MultipartFile): ResponseEntity<DocumentUploadResponse> {
        return ResponseEntity.ok(documentManager.temporarilyStoreDocumentAndTriggerStorage(pdfDocument))
    }

    override fun checkDocument(documentId: String): ResponseEntity<DocumentExistsResponse> {
        return ResponseEntity.ok(documentManager.checkIfDocumentExistsWithId(documentId))
    }

    override fun getDocument(documentId: String): ResponseEntity<InputStreamResource> {
        val document = documentManager.retrieveDocumentById(documentId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${document.title}")
            .body(document.content)
    }
}
