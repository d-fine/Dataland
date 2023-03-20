package org.dataland.documentmanager.controller

import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.api.DocumentApi
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DocumentController: DocumentApi {
    override fun getDocument(documentId: String): ResponseEntity<DocumentMetaInfo> {
        return ResponseEntity.ok(DocumentMetaInfo(documentId, "Test Document"))
    }
}
