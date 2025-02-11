package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DocumentStream
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.documentmanager.services.conversion.lowercaseExtension
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Implements the generation of document meta info, storage of the meta info temporarily locally
 * @param inMemoryDocumentStore the wrapper for the map of the saved in memory document meta info
 * @param documentMetaInfoRepository the repository for accessing the meta info database
 */
@Component
class DocumentManager
    @Autowired
    constructor(
        private val documentMetaInfoRepository: DocumentMetaInfoRepository,
        private val inMemoryDocumentStore: InMemoryDocumentStore,
        private val storageApi: StreamingStorageControllerApi,
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val fileProcessor: FileProcessor,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Stores the meta information of a document, saves it temporarily locally and notifies that it can be
         * retrieved from internal-storage for persistence
         * @param document the multipart file which contains the uploaded document
         * @param documentMetaInfo meta info for document provided by uploaded
         * @returns the meta information for the document
         */
        @Transactional(propagation = Propagation.NEVER)
        fun temporarilyStoreDocumentAndTriggerStorage(
            document: MultipartFile,
            documentMetaInfo: DocumentMetaInfo,
        ): DocumentUploadResponse {
            val correlationId = randomUUID().toString()
            logger.info("Started temporary storage process for document with correlation ID: $correlationId")

            val documentId = generateDocumentId(document, correlationId)
            if (this.checkIfDocumentExistsWithId(documentId)) {
                return documentMetaInfoRepository.getByDocumentId(documentId)?.toDocumentUploadResponse()
                    ?: throw ResourceNotFoundApiException(
                        summary = "Document with documentId $documentId could not be retrieved.",
                        message = "Document with documentId $documentId exists but could not be retrieved. CorrelationId: $correlationId",
                    )
            }
            val documentMetaInfoEntity =
                DocumentMetaInfoEntity(
                    documentId = documentId,
                    documentType = getDocumentType(document),
                    documentName = documentMetaInfo.documentName,
                    documentCategory = documentMetaInfo.documentCategory,
                    companyIds = documentMetaInfo.companyIds.toMutableList(),
                    uploaderId = DatalandAuthentication.fromContext().userId,
                    uploadTime = Instant.now().toEpochMilli(),
                    publicationDate = documentMetaInfo.publicationDate,
                    reportingPeriod = documentMetaInfo.reportingPeriod,
                    qaStatus = QaStatus.Pending,
                )

            val documentBody = fileProcessor.processFile(document, correlationId)
            this.saveMetaInfoToDatabase(documentMetaInfoEntity, correlationId)
            inMemoryDocumentStore.storeDataInMemory(documentId, documentBody)
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                documentId, MessageType.DOCUMENT_RECEIVED, correlationId, ExchangeName.DOCUMENT_RECEIVED,
            )
            return DocumentUploadResponse(
                documentId,
                documentName = documentMetaInfoEntity.documentName,
                documentCategory = documentMetaInfoEntity.documentCategory,
                companyIds = documentMetaInfoEntity.companyIds,
                publicationDate = documentMetaInfoEntity.publicationDate,
                reportingPeriod = documentMetaInfoEntity.reportingPeriod,
            )
        }

        private fun getDocumentType(document: MultipartFile): DocumentType {
            val documentExtension = document.lowercaseExtension()
            return when (documentExtension) {
                "xls" -> DocumentType.Xls
                "xlsx" -> DocumentType.Xlsx
                "ods" -> DocumentType.Ods
                else -> DocumentType.Pdf
            }
        }

        /**
         * A wrapper for storing document meta information to the database immediately
         * @param documentMetaInfoEntity the document meta information to store
         */
        fun saveMetaInfoToDatabase(
            documentMetaInfoEntity: DocumentMetaInfoEntity,
            correlationId: String,
        ) {
            logger.info("Saving meta info of document with correlation ID: $correlationId")
            documentMetaInfoRepository.save(documentMetaInfoEntity)
        }

        private fun generateDocumentId(
            document: MultipartFile,
            correlationId: String,
        ): String {
            logger.info("Generate document meta info for document with correlation ID: $correlationId")
            val documentId = document.bytes.sha256()
            logger.info("Generated hash/document ID: $documentId for document with correlation ID: $correlationId. ")
            return documentId
        }

        /**
         * This method checks whether a document is already stored in the database or not
         * @param documentId the documentId of the document to be checked
         * @returns true if the document exists, false otherwise
         */
        fun checkIfDocumentExistsWithId(documentId: String): Boolean {
            logger.info("Check if document exists with ID: $documentId")
            val documentExists = documentMetaInfoRepository.existsById(documentId)
            if (documentExists) {
                logger.info("Document with ID: $documentId exists")
            } else {
                logger.info("Document with ID: $documentId does not exist")
            }
            return documentExists
        }

        /**
         * This method retrieves a document from the storage
         * @param documentId the documentId of the document to be retrieved
         */
        fun retrieveDocumentById(documentId: String): DocumentStream {
            val correlationId = randomUUID().toString()
            val metaDataInfoEntity =
                documentMetaInfoRepository.findById(documentId).orElseThrow {
                    ResourceNotFoundApiException(
                        "No document found",
                        "No document with ID: $documentId could be found. Correlation ID: $correlationId",
                    )
                }
            if (metaDataInfoEntity.qaStatus != QaStatus.Accepted) {
                throw ResourceNotFoundApiException(
                    "No accepted document found",
                    "A non-quality-assured document with ID: $documentId was found. " +
                        "Only quality-assured documents can be retrieved. Correlation ID: $correlationId",
                )
            }
            val documentDataStream = retrieveDocumentDataStream(documentId, correlationId)
            return DocumentStream(documentId, metaDataInfoEntity.documentType, documentDataStream)
        }

        private fun retrieveDocumentDataStream(
            documentId: String,
            correlationId: String,
        ): InputStreamResource {
            val inMemoryStoredDocument = inMemoryDocumentStore.retrieveDataFromMemoryStore(documentId)
            return if (inMemoryStoredDocument != null) {
                logger.info("Received document $documentId from temporary storage")
                InputStreamResource(ByteArrayInputStream(inMemoryStoredDocument))
            } else {
                logger.info("Received document $documentId from storage service")
                InputStreamResource(storageApi.getBlobFromInternalStorage(documentId, correlationId))
            }
        }

        /**
         * Update the document meta information with [documentId].
         * Fields 'companyIds' and 'reportingPeriods' will be extended instead
         * of overwritten.
         * @param documentId identifier of document to be patched
         * @param documentMetaInfoPatch meta data patch object
         * @return DocumentUploadResponse object to be sent to patching user
         */
        fun patchDocumentMetaInformation(
            documentId: String,
            documentMetaInfoPatch: DocumentMetaInfoPatch,
        ): DocumentUploadResponse {
            val correlationId = randomUUID().toString()
            logger.info("Retrieve document with document ID $documentId from storage. Correlation ID: $correlationId.")
            if (!checkIfDocumentExistsWithId(documentId)) {
                throw ResourceNotFoundApiException(
                    summary = "Document with ID $documentId does not exist.",
                    message = "Document with ID $documentId does not exist. Correlation ID: $correlationId.",
                )
            }
            val documentMetaInfoEntity =
                documentMetaInfoRepository.getByDocumentId(documentId)
                    ?: throw ResourceNotFoundApiException(
                        summary = "Document with ID $documentId could not be retrieved.",
                        message = "Document with ID $documentId could not be retrieved. Correlation ID: $correlationId.",
                    )

            logger.info("Updating meta information for document with ID $documentId. CorrelationID: $correlationId.")

            documentMetaInfoPatch.documentName?.let { documentMetaInfoEntity.documentName = it }
            documentMetaInfoPatch.documentCategory?.let { documentMetaInfoEntity.documentCategory = it }
            documentMetaInfoPatch.companyIds?.let {
                documentMetaInfoEntity.companyIds.addAll(documentMetaInfoPatch.companyIds)
            }
            documentMetaInfoPatch.publicationDate?.let { documentMetaInfoEntity.publicationDate = it }
            documentMetaInfoPatch.reportingPeriod?.let { documentMetaInfoEntity.reportingPeriod = it }

            return documentMetaInfoRepository.save(documentMetaInfoEntity).toDocumentUploadResponse()
        }

        /**
         * Update the document meta information with [documentId].
         * This version only adds a single companyId to the companyIds list.
         * @param documentId identifier of document to be patched
         * @param companyId the company id to add
         * @return DocumentUploadResponse object to be sent to patching user
         */
        fun patchDocumentMetaInformationCompanyIds(
            documentId: String,
            companyId: String,
        ): DocumentUploadResponse {
            val correlationId = randomUUID().toString()
            val documentMetaInfoEntity = retrieveDocumentById(documentId, correlationId)

            logger.info("Updating company ids for document with ID $documentId. CorrelationID: $correlationId.")

            documentMetaInfoEntity.companyIds.add(companyId)

            return documentMetaInfoRepository.save(documentMetaInfoEntity).toDocumentUploadResponse()
        }

        private fun retrieveDocumentById(
            documentId: String,
            correlationId: String,
        ): DocumentMetaInfoEntity {
            logger.info("Retrieve document with document ID $documentId from storage. Correlation ID: $correlationId.")
            if (!checkIfDocumentExistsWithId(documentId)) {
                throw ResourceNotFoundApiException(
                    summary = "Document with ID $documentId does not exist.",
                    message = "Document with ID $documentId does not exist. Correlation ID: $correlationId.",
                )
            }
            return documentMetaInfoRepository.getByDocumentId(documentId)
                ?: throw ResourceNotFoundApiException(
                    summary = "Document with ID $documentId could not be retrieved.",
                    message = "Document with ID $documentId could not be retrieved. Correlation ID: $correlationId.",
                )
        }
    }
