package org.dataland.documentmanager.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.dataland.documentmanager.services.DocumentManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
) : DocumentApi {
    override fun postDocument(
        document: MultipartFile,
        documentMetaInfo: DocumentMetaInfo?,
    ): ResponseEntity<DocumentMetaInfoResponse> {
        documentMetaInfo?.companyIds?.forEach { isCompanyIdValid(it) }
        return ResponseEntity.ok(documentManager.temporarilyStoreDocumentAndTriggerStorage(document, documentMetaInfo))
    }

    override fun checkDocument(documentId: String) {
        if (!documentManager.checkIfDocumentExists(documentId)) {
            throw ResourceNotFoundApiException(
                "Document with ID $documentId does not exist.",
                "Document with ID $documentId does not exist.",
            )
        }
    }

    override fun getDocument(documentId: String): ResponseEntity<InputStreamResource> {
        val document = documentManager.retrieveDocument(documentId)
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

    override fun patchDocumentMetaInfo(
        documentId: String,
        documentMetaInfoPatch: DocumentMetaInfoPatch,
    ): ResponseEntity<DocumentMetaInfoResponse> {
        if (documentMetaInfoPatch.isNullOrEmpty()) {
            throw InvalidInputApiException(
                summary = "DocumentMetaInfoPatch must not be null or empty.",
                message = "DocumentMetaInfoPatch must not be null or empty. Please provide data.",
            )
        }
        documentMetaInfoPatch.companyIds?.forEach { isCompanyIdValid(it) }
        return ResponseEntity.ok(
            documentManager.patchDocumentMetaInformation(documentId, documentMetaInfoPatch),
        )
    }

    override fun patchDocumentMetaInfoCompanyIds(
        documentId: String,
        companyId: String,
    ): ResponseEntity<DocumentMetaInfoResponse> {
        isCompanyIdValid(companyId)
        return ResponseEntity.ok(
            documentManager.patchDocumentMetaInformationCompanyIds(documentId, companyId),
        )
    }

    /**
     * Checks if passed companyId is valid by calling respective HEAD endpoint in backend companyDataController
     * @param companyId
     * @return returns true if companyId is valid
     */
    private fun isCompanyIdValid(companyId: String): Boolean {
        try {
            companyDataControllerApi.isCompanyIdValid(companyId)
            return true
        } catch (exception: ClientException) {
            if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    summary = "Company with CompanyId $companyId not found.",
                    message = "Company with CompanyId $companyId not found.",
                )
            } else {
                throw exception
            }
        }
    }
}
