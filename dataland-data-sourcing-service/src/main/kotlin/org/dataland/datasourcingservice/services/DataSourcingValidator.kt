package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.datalanddocumentmanager.openApiClient.infrastructure.ClientException
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Validates whether a list of company identifiers exist in Dataland and retrieves their UUIDs.
         *
         * @param identifierList List of company identifier strings to check.
         * @return List of UUIDs for each identifier in the input; a `null` entry indicates a non-existing company.
         */
        private fun validateAndGetCompanyIds(identifierList: List<String>): List<UUID?> {
            logger.info("Validating company ids for $identifierList")
            return companyDataControllerApi
                .postCompanyValidation(identifierList)
                .map { it.companyInformation?.companyId?.let { id -> UUID.fromString(id) } }
        }

        /**
         * Validates if a document with the provided id exists on Dataland.
         * @param documentId the id of a document
         * @throws ResourceNotFoundApiException if no document is associated to the provided id
         */
        fun validateDocumentId(documentId: String) {
            logger.info("Validating document ID for $documentId")
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
         * Validates the format of each reporting period in the provided list.
         *
         * @param reportingPeriods List of reporting period strings (expected format: "YYYY" or "YYYY-QX").
         * @return List of Boolean values indicating if each reporting period matches the expected format.
         */
        private fun validateReportingPeriods(reportingPeriods: List<String>): List<Boolean> {
            logger.info("Validating reporting periods for $reportingPeriods")
            return reportingPeriods.map { reportingPeriod ->
                ValidationUtils.isReportingPeriod(reportingPeriod)
            }
        }

        /**
         * Validates whether each data type in the provided list is recognized by Dataland.
         *
         * @param dataTypes List of data type strings to validate.
         * @return List of Boolean values indicating if each data type is valid (recognized).
         */
        private fun validateDataTypes(dataTypes: List<String>): List<Boolean> {
            logger.info("Validating data types for $dataTypes")
            return dataTypes.map { dataType ->
                DataTypeEnum.decode(dataType) != null
            }
        }

        /**
         * Validates a bulk data request consisting of multiple company IDs, reporting periods, and data types.
         *
         * @param bulkDataRequest The request object containing sets of company identifiers, reporting periods, and data types.
         * @return Result object containing the validation status for company IDs, data types, and reporting periods.
         */
        fun validateBulkDataRequest(bulkDataRequest: BulkDataRequest): DataRequestValidationResult =
            validateRequestData(
                companyIds = bulkDataRequest.companyIdentifiers.toList(),
                reportingPeriods = bulkDataRequest.reportingPeriods.toList(),
                dataTypes = bulkDataRequest.dataTypes.toList(),
            )

        /**
         * Validates a single data sourcing request, ensuring that the company identifier, data type, and reporting period are all valid.
         *
         * This method performs the following checks:
         * - Verifies that the company identifier exists in the system.
         * - Checks that the data type is recognized and supported.
         * - Checks that the reporting period is in a valid format and allowed range.
         *
         * If any of the above validations fail, all error messages are accumulated and thrown together as a single
         * `IllegalArgumentException`, so the caller gets full feedback in one go.
         *
         * @param singleRequest The request object containing the company identifier, data type, and reporting period to validate.
         * @return The UUID of the validated company if all validations succeed.
         * @throws IllegalArgumentException if one or more input values are invalid;
         * the exception message provides a summary of all issues found.
         */
        fun validateSingleDataRequest(singleRequest: SingleRequest): UUID {
            val errors = mutableListOf<String>()
            val companyIdValidationResult =
                validateAndGetCompanyIds(
                    listOf(singleRequest.companyIdentifier),
                ).first()
            if (companyIdValidationResult == null) {
                errors.add("The company identifier ${singleRequest.companyIdentifier} does not exist on Dataland.")
            }
            val reportingPeriodValidationResult =
                validateReportingPeriods(
                    listOf(singleRequest.reportingPeriod),
                ).first()
            if (!reportingPeriodValidationResult) {
                errors.add("The reporting period ${singleRequest.reportingPeriod} is invalid.")
            }
            val dataTypeValidationResult =
                validateDataTypes(
                    listOf(singleRequest.dataType),
                ).first()
            if (!dataTypeValidationResult) {
                errors.add("The data type ${singleRequest.dataType} is invalid.")
            }
            if (errors.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Invalid input data.",
                    message = errors.joinToString(" "),
                )
            }
            return companyIdValidationResult!!
        }

        /**
         * Core validation functionality that checks company IDs, data types, and reporting periods.
         *
         * @param companyIds List of company identifier strings.
         * @param reportingPeriods List of reporting period strings.
         * @param dataTypes List of data type strings.
         * @return DataRequestValidationResult object containing lists of validation results for each input.
         */
        private fun validateRequestData(
            companyIds: List<String>,
            reportingPeriods: List<String>,
            dataTypes: List<String>,
        ): DataRequestValidationResult {
            val companyIdResult =
                companyIds.zip(validateAndGetCompanyIds(companyIds)).map { (companyId, result) ->
                    mapOf(companyId to result)
                }
            val dataTypeResult =
                dataTypes.zip(validateDataTypes(dataTypes)).map { (dataType, result) -> mapOf(dataType to result) }
            val reportingPeriodResult =
                reportingPeriods.zip(validateReportingPeriods(reportingPeriods)).map { (reportingPeriod, result) ->
                    mapOf(
                        reportingPeriod to result,
                    )
                }
            return DataRequestValidationResult(
                companyIdResult, dataTypeResult,
                reportingPeriodResult,
            )
        }

        /**
         * Encapsulates the results of validating a data sourcing request.
         *
         * @property companyIdValidation List of maps associating input company ID strings with their
         * corresponding UUIDs (or null if not found).
         * @property dataTypeValidation List of maps associating input data type strings with their
         * Boolean validation result.
         * @property reportingPeriodValidation List of maps associating input reporting period
         * strings with their Boolean validation result.
         */
        data class DataRequestValidationResult(
            val companyIdValidation: List<Map<String, UUID?>>,
            val dataTypeValidation: List<Map<String, Boolean>>,
            val reportingPeriodValidation: List<Map<String, Boolean>>,
        )
    }
