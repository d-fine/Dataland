package org.dataland.documentmanager.controller

import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.services.DocumentManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class DocumentController(
    @Autowired val documentManager: DocumentManager,
): DocumentApi {
    override fun postDocument(pdfDocument: MultipartFile): ResponseEntity<DocumentMetaInfo> {
        return ResponseEntity.ok(documentManager.temporarilyStoreDocumentAndTriggerStorage(pdfDocument))
    }
}
