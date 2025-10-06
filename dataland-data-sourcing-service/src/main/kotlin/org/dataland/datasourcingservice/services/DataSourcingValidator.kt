package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
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
        private fun validateCompanyId(identifier: String): UUID {
            val companyInformation =
                companyDataControllerApi.postCompanyValidation(listOf(identifier)).firstOrNull()?.companyInformation
                    ?: throw ResourceNotFoundApiException(
                        "The company identifier is unknown.",
                        "No company is associated to the identifier $identifier.",
                    )
            return UUID.fromString(companyInformation.companyId)
        }

        /**
         * Validates if a company identifier exists on Dataland and returns the associated company id wrapped in a Result.
         * @param identifier the identifier of a company
         * @return a Result containing the UUID of the company associated to the provided identifier if it exists,
         *         or a ResourceNotFoundApiException if no company is associated to the provided identifier
         */
        fun validateAndGetCompanyId(identifier: String): Result<UUID> =
            try {
                Result.success(validateCompanyId(identifier))
            } catch (e: ResourceNotFoundApiException) {
                Result.failure(e)
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

        /**
         * * Validates if a reporting period matches the expected format (YYYY or YYYY-QX).
         * @param reportingPeriod the reporting period to validate
         * @return a Result containing the reporting period if it matches the expected format,
         *         or an IllegalArgumentException if it does not match the expected format
         */
        fun validateReportingPeriod(reportingPeriod: String): Result<String> =
            if (ValidationUtils.isReportingPeriod(reportingPeriod)) {
                Result.success(reportingPeriod) // Success if it matches the format
            } else {
                Result.failure(IllegalArgumentException("Invalid reporting period: $reportingPeriod"))
            }

        /**
         * Validates a list of data dimension tuples by checking if the company ID and reporting period are valid.
         * @param listOfRequestedDataDimensionTuples the list of data dimension tuples to validate
         * @return a Pair containing two lists:
         *         - The first list contains all properly validated data dimension tuples.
         *         - The second list contains all data dimension tuples that failed validation.
         */
        fun validateBulkDataRequest(
            listOfRequestedDataDimensionTuples: List<BasicDataDimensions>,
        ): Pair<List<BasicDataDimensions>, List<BasicDataDimensions>> {
            val validationResults =
                listOfRequestedDataDimensionTuples.map { dataDimension ->
                    val companyIdResult = validateAndGetCompanyId(dataDimension.companyId)
                    val reportingPeriodResult = validateReportingPeriod(dataDimension.reportingPeriod)
                    DataDimensionValidationResult(dataDimension, companyIdResult, reportingPeriodResult)
                }

            // Partition validated and invalid requests
            val (validated, invalid) =
                validationResults.partition { result ->
                    result.companyIdValidation.isSuccess && result.reportingPeriodValidation.isSuccess
                }

            // Map validated requests: Apply validated companyId
            val validatedRequests =
                validated.map { result ->
                    val validatedCompanyId = result.companyIdValidation.getOrThrow()
                    BasicDataDimensions(
                        companyId = validatedCompanyId.toString(),
                        dataType = result.dataDimension.dataType,
                        reportingPeriod = result.dataDimension.reportingPeriod,
                    )
                }

            // Map invalid requests: Just extract the original data dimensions
            val invalidRequests =
                invalid.map { result ->
                    result.dataDimension
                }
            return Pair(validatedRequests, invalidRequests)
        }

        /**
         * Data class representing the result of validating a data dimension tuple.
         * @param dataDimension the original data dimension tuple
         * @param companyIdValidation the result of validating the company ID
         * @param reportingPeriodValidation the result of validating the reporting period
         */
        private data class DataDimensionValidationResult(
            val dataDimension: BasicDataDimensions,
            val companyIdValidation: Result<UUID>,
            val reportingPeriodValidation: Result<String>,
        )
    }
