package org.dataland.documentmanager.controller

import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.services.DocumentManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class DocumentController: DocumentApi {
    override fun getDocument(documentId: String): ResponseEntity<DocumentMetaInfo> {
        return ResponseEntity.ok(DocumentMetaInfo(documentId, "Test Document"))
    }
}
