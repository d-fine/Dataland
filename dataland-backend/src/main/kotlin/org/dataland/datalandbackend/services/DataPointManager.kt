package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.dataland.datalandbackend.utils.IdUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.net.URI
import java.time.Instant
import java.util.UUID

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component("DataPointManager")
class DataPointManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataManager: DataManager,
    @Value("\${dataland.specification-service.base-url}")
    private val specificationServiceBaseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    // ToDo: Implement proper logging

    private fun getJsonNodeFromUrl(url: String): JsonNode = ObjectMapper().readTree(URI(url).toURL())

    private fun constructDataPointUrl(dataPoint: String): String = "$specificationServiceBaseUrl/datapointSpecifications/$dataPoint.json"

    private fun constructDataTypeUrl(dataType: String): String = "$specificationServiceBaseUrl/datapointTypes/$dataType.json"

    private fun constructFrameworkTemplateUrl(framework: String): String =
        "$specificationServiceBaseUrl/frameworks/templates/$framework.json"

    private fun getDataType(dataPoint: String): String {
        val dataTypeUrl = getJsonNodeFromUrl(constructDataPointUrl(dataPoint)).get("datapointType").asText()
        return dataTypeUrl.split("/").last().replace(".json", "")
    }

    fun getFrameworkTemplate(framework: String): JsonNode {
        val frameworkUrl = constructFrameworkTemplateUrl(framework)
        checkIfSpecificationExists(frameworkUrl)
        return getJsonNodeFromUrl(frameworkUrl)
    }

    fun getJsonNodeFromString(json: String): JsonNode = ObjectMapper().readTree(json)

    fun processDataSet(
        uploadedData: StorableDataSet,
        bypassQa: Boolean,
        correlationId: String,
    ): String {
        val frameworkTemplate = getFrameworkTemplate(uploadedData.dataType)
        logger.info("Extract the expected data points and their respective JSON path.")
        val expectedDataPoints = extractDataPointsFromFrameworkTemplate(frameworkTemplate, "")
        val dataJson = getJsonNodeFromString(uploadedData.data)

        val dataSetId = IdUtils.generateUUID()
        dataManager.storeMetaDataFrom(
            dataId = dataSetId,
            storableDataSet = uploadedData,
            correlationId = correlationId,
        )

        val dataIds = mutableListOf<String>()
        expectedDataPoints.forEach {
            val dataPointValue = getValueFromJsonNode(dataJson, it.key)
            if (dataPointValue.isEmpty()) {
                logger.info("No value found for key ${it.key}")
                return@forEach
            }
            logger.info("Found value $dataPointValue for key ${it.key}")

            val dataId =
                storeDataPoint(
                    UploadableDataPoint(
                        data = dataPointValue,
                        datapointSpecification = expectedDataPoints.getValue(it.key),
                        companyId = UUID.fromString(uploadedData.companyId),
                        reportingPeriod = uploadedData.reportingPeriod,
                    ),
                    uploadedData.uploaderUserId,
                    bypassQa,
                    correlationId,
                )
            logger.info("Created storable data point for key-value pair ${it.key} and ${it.value} under id $dataId")
            dataIds += dataId
        }
        logger.info("Stored data points with ids $dataIds")
        return dataSetId
    }

    /**
     * Gets the string value of the JSON node identified by the (possibly) nested JSON path.
     * @param jsonNode The JSON node
     * @param jsonPath The JSON path identifying the value
     * @return The string representation of the value
     */
    fun getValueFromJsonNode(
        jsonNode: JsonNode,
        jsonPath: String,
    ): String {
        var currentNode = jsonNode
        jsonPath.split(".").forEach { path ->
            currentNode = currentNode.get(path) ?: return ""
        }
        return if (currentNode.isNull) {
            ""
        } else if (currentNode.isTextual) {
            currentNode.textValue()
        } else {
            currentNode.toString()
        }
    }

    fun extractDataPointsFromFrameworkTemplate(
        jsonNode: JsonNode,
        fieldName: String,
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()
        if (jsonNode.isObject) {
            val fields = jsonNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                val nextFieldName = if (fieldName.isEmpty()) jsonField.key else "$fieldName.${jsonField.key}"
                results += extractDataPointsFromFrameworkTemplate(jsonField.value, nextFieldName)
            }
            // ToDo: Reconsider if these if-else checks are sufficient
        } else if (jsonNode.toString() == "null" || jsonNode.isArray) {
            throw IllegalArgumentException("Framework template contains unexpected null or array values.")
        } else {
            logger.info("Found leaf node $fieldName with value $jsonNode")
            results[fieldName] = jsonNode.textValue()
        }

        return results
    }

    private fun checkIfDataPointDefinitionExists(dataPoint: String) {
        logger.info("Checkinpagig if data point definition exists for $dataPoint")
        checkIfSpecificationExists(constructDataPointUrl(dataPoint))
    }

    private fun checkIfSpecificationExists(specPath: String) {
        val url = URI(specPath).toURL()
        logger.info("Checking if specification exists at $url")
        try {
            getJsonNodeFromUrl(url.toString())
        } catch (e: FileNotFoundException) {
            logger.error("No specification for $specPath exists. Message: ${e.message}")
            // Todo: implement proper error message
            throw IllegalArgumentException("No specification for $specPath exists.")
        }
    }

    fun validateDataPoint(
        dataPoint: String,
        data: String,
    ) {
        checkIfDataPointDefinitionExists(dataPoint)
        val dataTypeUrl = constructDataTypeUrl(getDataType(dataPoint))
        checkIfSpecificationExists(dataTypeUrl)
        val validationClass = getJsonNodeFromUrl(dataTypeUrl).get("validatedBy").asText()
        validateConsistency(data, validationClass)
    }

    fun storeDataPoint(
        uploadedData: UploadableDataPoint,
        uploaderUserId: String,
        bypassQa: Boolean,
        correlationId: String,
    ): String {
        logger.info("Executing check for '${uploadedData.datapointSpecification}' data point (correlation ID: $correlationId).")
        validateDataPoint(uploadedData.datapointSpecification, uploadedData.data)
        logger.info("Storing '${uploadedData.datapointSpecification}' data point.")
        val uploadTime = Instant.now().toEpochMilli()
        val storableDataSet =
            StorableDataSet(
                companyId = uploadedData.companyId.toString(),
                dataType = uploadedData.datapointSpecification,
                uploaderUserId = uploaderUserId,
                uploadTime = uploadTime,
                reportingPeriod = uploadedData.reportingPeriod,
                data = uploadedData.data,
            )

        val dataId = UUID.randomUUID()
        dataManager.storeMetaDataFrom(
            dataId = dataId.toString(),
            storableDataSet = storableDataSet,
            correlationId = correlationId,
        )
        dataManager.storeDataSetInTemporaryStoreAndSendMessage(dataId.toString(), storableDataSet, bypassQa, correlationId)

        return dataId.toString()
    }

    fun retrieveDataPoint(
        dataId: UUID,
        dataType: String,
        correlationId: String,
    ): StorableDataSet {
        logger.info("Retrieving data point with id $dataId")
        val storedDataPoint = dataManager.getPublicDataSet(dataId.toString(), dataType, correlationId)
        return storedDataPoint
    }

    fun validateConsistency(
        jsonData: String,
        className: String,
    ) {
        val classForValidation = Class.forName(className).kotlin.java
        val validator = Validation.buildDefaultValidatorFactory().validator
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        val dataPointObject = objectMapper.readValue(jsonData, classForValidation)
        val violations = validator.validate(dataPointObject)
        if (violations.isNotEmpty()) {
            // ToDo: properly handle the exception and the associated messages
            logger.error("Validation failed when casting $jsonData into $className")
            var errorMessage = "Validation failed for data point of type $className: "
            violations.forEach {
                logger.error(it.message)
                errorMessage += (it.message)
            }

            throw IllegalArgumentException(errorMessage)
        }
    }
}
