package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Class for validating data points
 * @param objectMapper the object mapper to use for JSON operations
 * @param specificationClient the specification client to use for fetching specifications
 */

@Component("DataPointValidator")
class DataPointValidator
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val specificationClient: SpecificationControllerApi,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Validates a single data point by casting it to the correct class and running the validations
         * @param dataPointType the type of the data point to validate
         * @param dataPoint the data to validate
         * @param correlationId the correlation id for the operation
         */
        fun validateDataPoint(
            dataPointType: String,
            dataPoint: String,
            correlationId: String,
        ) {
            logger.info("Validating data point $dataPointType (correlation ID: $correlationId)")
            validateDataPointTypeExists(dataPointType)
            val dataPointBaseTypeId = specificationClient.getDataPointTypeSpecification(dataPointType).dataPointBaseType.id
            val validationClass = specificationClient.getDataPointBaseType(dataPointBaseTypeId).validatedBy
            validateConsistency(dataPoint, validationClass, correlationId)
        }

        /**
         * Validates with the specification service that a data point identifier exists
         * @param dataPointType the identifier to validate
         */
        fun validateDataPointTypeExists(dataPointType: String) {
            try {
                specificationClient.getDataPointTypeSpecification(dataPointType)
            } catch (clientException: ClientException) {
                logger.error("Data point identifier $dataPointType not found: ${clientException.message}.")
                throw InvalidInputApiException(
                    "Specified data point identifier $dataPointType is not valid.",
                    "The specified data point identifier $dataPointType is not known to the specification service.",
                )
            }
        }

        /**
         * Validates the consistency of a JSON string with a given class.
         * @param jsonData The JSON string to validate
         * @param className The name of the class to validate against
         * @param correlationId The correlation ID of the operation
         */
        fun validateConsistency(
            jsonData: String,
            className: String,
            correlationId: String,
        ) {
            if (!className.startsWith("org.dataland.datalandbackend.model.datapoints.")) {
                logger.error("Invalid class name $className (correlation ID: $correlationId).")
                throw InvalidInputApiException(
                    "Invalid class name.",
                    "The class name $className is not valid.",
                )
            }
            val classForValidation = Class.forName(className).kotlin.java
            val dataPointObject = checkCastIntoClass(jsonData, classForValidation, className, correlationId)
            checkForViolations(dataPointObject, className, correlationId)
        }

        /**
         * Validates the dataset by checking the data points and referenced reports
         * @param datasetContent the content of the dataset
         * @param referencedReports the referenced reports
         * @param correlationId the correlation id for the operation
         */
        fun validateDataset(
            datasetContent: Map<String, JsonSpecificationLeaf>,
            referencedReports: Map<String, CompanyReport>?,
            correlationId: String,
        ) {
            referencedReportsUtilities.validateReferencedReportConsistency(referencedReports ?: emptyMap())
            val observedDocumentReferences = mutableSetOf<String>()

            datasetContent.forEach { (dataPointType, dataPointJsonLeaf) ->
                val dataPoint = objectMapper.writeValueAsString(dataPointJsonLeaf.content)
                if (dataPoint.isEmpty()) return@forEach
                validateDataPoint(dataPointType, dataPoint, correlationId)

                val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(dataPoint)
                if (companyReport != null && referencedReports != null) {
                    observedDocumentReferences.add(companyReport.fileReference)
                    referencedReportsUtilities.validateReportConsistencyWithGlobalList(
                        companyReport,
                        referencedReports,
                    )
                }
            }

            if (referencedReports != null) {
                val expectedObservedReferences = referencedReports.values.map { it.fileReference }.toSet()
                val unusedReferences = expectedObservedReferences - observedDocumentReferences
                if (unusedReferences.isNotEmpty()) {
                    throw InvalidInputApiException(
                        "Mismatching document references",
                        "The following document references were not used " +
                            "but listed in the referenced report field: $unusedReferences",
                    )
                }
            }
        }

        /**
         * Checks if the JSON data can be cast into a given class
         * @param jsonData The JSON data to check
         * @param classForValidation The class to check against
         * @param className The name of the class to check against
         * @param correlationId The correlation ID of the operation
         */
        private fun checkCastIntoClass(
            jsonData: String,
            classForValidation: Class<out Any>,
            className: String,
            correlationId: String,
        ): Any {
            try {
                return objectMapper.readValue(jsonData, classForValidation)
            } catch (ex: JsonMappingException) {
                logger.error("Unable to cast JSON data $jsonData into $className (correlation ID: $correlationId): ${ex.message}")
                throw InvalidInputApiException(
                    summary = "Validation failed for data point.",
                    message = ex.message ?: "Validation failed for data point.",
                    cause = ex,
                )
            } catch (ex: JsonParseException) {
                logger.error("Unable to parse JSON data $jsonData into $className (correlation ID: $correlationId): ${ex.message}")
                throw InvalidInputApiException(
                    summary = "Validation failed for data point due to invalid JSON",
                    message = "Validation failed for data point due to ${ex.message}.",
                    cause = ex,
                )
            }
        }

        /**
         * Checks for violations in the data point object
         * @param dataPointObject The object to check
         * @param className The name of the class to check against
         * @param correlationId The correlation ID of the operation
         */
        private fun checkForViolations(
            dataPointObject: Any?,
            className: String,
            correlationId: String,
        ) {
            val validator = Validation.buildDefaultValidatorFactory().validator
            val violations = validator.validate(dataPointObject)
            if (violations.isNotEmpty()) {
                logger.error("Validation failed for data point of type $className (correlation ID: $correlationId): $violations")
                throw InvalidInputApiException(
                    summary = "Validation failed for data point.",
                    message = "Validation failed for data point: ${violations.joinToString(", ") { it.message }}",
                )
            }
        }
    }
