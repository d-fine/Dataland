package org.dataland.documentmanager.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.documentmanager.api.DocumentApi
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.dataland.documentmanager.services.DocumentManager
import org.dataland.documentmanager.services.DocumentMetaInformationSearchFilter
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

    override fun getDocumentMetaInformation(documentId: String): ResponseEntity<DocumentMetaInfoEntity> =
        ResponseEntity.ok(documentManager.retrieveDocumentMetaInfo(documentId))

    override fun patchDocumentMetaInfo(
        documentId: String,
        documentMetaInfoPatch: DocumentMetaInfoPatch,
    ): ResponseEntity<DocumentMetaInfoResponse> {
        if (documentMetaInfoPatch.isNullOrEmpty()) {
            throw InvalidInputApiException(
                summary = "DocumentMetaInfoPatch must not be empty. At least one parameter should be filled.",
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

    /**
     * Searches in storage for document meta information matching the non-null search
     * parameter values and optionally only returns a chunk of the search results.
     * Only document meta information with QaStatus "Accepted" is returned, and results
     * are ordered by publication date in reverse chronological order.
     * @param companyId The company ID to filter by, ignored if null.
     * @param documentCategory The document category to filter by, ignored if null.
     * @param reportingPeriod The reporting period to filter by, ignored if null.
     * @param chunkSize The maximum size of the chunk of search results returned. If
     * null, all search results are returned.
     * @param chunkIndex The index, counting started at 0, of the chunk that shall be
     * returned. If chunkSize is null, there is only a single chunk, so chunkIndex
     * must be 0 then.
     * @return A ResponseEntity wrapping a list of DocumentUploadResponse objects. In
     * addition to the values of the three potential search parameter fields, these
     * contain the document ids and names as well as publication dates.
     */
    override fun searchForDocumentMetaInformation(
        companyId: String?,
        documentCategory: DocumentCategory?,
        reportingPeriod: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<DocumentMetaInfoResponse>> {
        if (chunkSize <= 0) {
            throw InvalidInputApiException(
                summary = "Invalid chunk size.",
                message = "Chunk size must be positive.",
            )
        }
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                companyId,
                documentCategory,
                reportingPeriod,
            )
        if (documentMetaInformationSearchFilter.isEmpty()) {
            throw InvalidInputApiException(
                summary = "At least one search parameter must be non-null.",
                message = "At least one search parameter must be non-null.",
            )
        }
        return ResponseEntity.ok(
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
                chunkSize,
                chunkIndex,
            ),
        )
    }
}
