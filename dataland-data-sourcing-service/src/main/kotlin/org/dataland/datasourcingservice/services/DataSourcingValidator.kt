package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.datalanddocumentmanager.openApiClient.infrastructure.ClientException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class that manages all operations related to data sourcing validation.
 */
@Service("DataSourcingValidator")
class DataSourcingValidator
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val documentControllerApi: DocumentControllerApi,
    ) {
        /**
         * Validates if a company identifier exists on Dataland and returns the associated company id.
         * @param identifier the identifier of a company
         * @return the UUID of the company associated to the provided identifier
         * @throws ResourceNotFoundApiException if no company is associated to the provided identifier
         */
        fun validateAndGetCompanyIdForIdentifier(identifier: String): UUID {
            val companyInformation =
                companyDataControllerApi.postCompanyValidation(listOf(identifier)).firstOrNull()?.companyInformation
                    ?: throw ResourceNotFoundApiException(
                        "The company identifier is unknown.",
                        "No company is associated to the identifier $identifier.",
                    )
            return UUID.fromString(companyInformation.companyId)
        }

        /**
         * Validates if a document with the provided id exists on Dataland.
         * @param documentId the id of a document
         * @throws ResourceNotFoundApiException if no document is associated to the provided id
         */
        fun validateDocumentId(documentId: String) {
            try {
                documentControllerApi.checkDocument(documentId)
            } catch (_: ClientException) {
                throw ResourceNotFoundApiException(
                    summary = "Document with id $documentId not found.",
                    message = "The document with id $documentId does not exist on Dataland.",
                )
            }
        }
    }
