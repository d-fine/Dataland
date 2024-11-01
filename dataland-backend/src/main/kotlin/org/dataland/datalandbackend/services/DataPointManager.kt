package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientError
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component("DataPointManager")
class DataPointManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataManager: DataManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val specificationManager: SpecificationControllerApi,
    @Autowired private val datasetDatapointRepository: DatasetDatapointRepository,
    @Autowired private val exceptionForwarder: ExceptionForwarder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    // ToDo: Implement proper logging

    fun retrieveDataPoint(
        dataId: UUID,
        dataType: String,
        correlationId: String,
    ): StorableDataSet {
        logger.info("Retrieving data point with id $dataId")
        val storedDataPoint = dataManager.getPublicDataSet(dataId.toString(), dataType, correlationId)
        return storedDataPoint
    }

    fun getFrameworkTemplate(framework: String): JsonNode =
        try {
            getJsonNodeFromString(specificationManager.getFrameworkSpecification(framework).schema)
        } catch (clientException: ClientException) {
            logger.info("Framework $framework not found.")
            throw clientException
        }

    fun getJsonNodeFromString(json: String): JsonNode = ObjectMapper().readTree(json)

    fun processDataSet(
        uploadedData: StorableDataSet,
        bypassQa: Boolean,
        correlationId: String,
    ): String {
        logger.info("Get Template for framework ${uploadedData.dataType}")
        val frameworkTemplate = getFrameworkTemplate(uploadedData.dataType)
        logger.info("Extract the expected data points and their respective JSON path.")
        logger.info(frameworkTemplate.toPrettyString())

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
        this.datasetDatapointRepository.save(
            DatasetDatapointEntity(dataId = dataSetId, dataPoints = dataIds.joinToString(",")),
        )
        logger.info("Stored data points with ids $dataIds")
        return dataSetId
    }

    fun replaceFieldInTemplate(
        frameworkTemplate: JsonNode,
        fullFieldName: String,
        currentJsonPath: String,
        replacementValue: JsonNode,
    ) {
        val simpleFieldName = fullFieldName.split(".").last()
        val expectedFullPath = "$currentJsonPath.$simpleFieldName"

        if (frameworkTemplate.has(simpleFieldName) && expectedFullPath == fullFieldName) {
            (frameworkTemplate as ObjectNode).set<JsonNode?>(simpleFieldName, replacementValue)
        } else if (frameworkTemplate.isObject) {
            val fields = frameworkTemplate.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                val jsonPath = if (currentJsonPath.isEmpty()) jsonField.key else "$currentJsonPath.${jsonField.key}"
                replaceFieldInTemplate(jsonField.value, fullFieldName, jsonPath, replacementValue)
            }
        }
    }

    fun getDataSetFromId(
        dataSetId: String,
        framework: String,
        correlationId: String,
    ): String {
        val dataPoints =
            datasetDatapointRepository
                .findById(dataSetId)
                .getOrNull()
                ?.dataPoints
                ?.split(",")
                ?: throw IllegalArgumentException("No data set found for id $dataSetId")
        return assembleDataSetFromDataPoints(dataPoints, framework, correlationId)
    }

    fun assembleDataSetFromDataPoints(
        dataPointIds: List<String>,
        framework: String,
        correlationId: String,
    ): String {
        val dataPoints = mutableListOf<String>()
        val frameworkTemplate = getFrameworkTemplate(framework)
        val allDataPointsInTemplate = extractDataPointsFromFrameworkTemplate(frameworkTemplate, "")

        dataPointIds.forEach { dataPoint ->
            val currentDataPoint = metaDataManager.getDataMetaInformationByDataId(dataPoint).dataType
            logger.info("Retrieving data with id $dataPoint and data point $currentDataPoint")
            if (!allDataPointsInTemplate.containsValue(currentDataPoint)) {
                throw IllegalArgumentException("Data point $currentDataPoint is not part of the framework template.")
            }
            dataPoints.add(currentDataPoint)
            val dataPointData = retrieveDataPoint(UUID.fromString(dataPoint), currentDataPoint, correlationId).data
            val replacementValue = getJsonNodeFromString(dataPointData)

            val jsonPaths = allDataPointsInTemplate.filterValues { it == currentDataPoint }.keys
            jsonPaths.forEach {
                replaceFieldInTemplate(frameworkTemplate, it, "", replacementValue)
            }
        }

        allDataPointsInTemplate.forEach {
            if (!dataPoints.contains(it.value)) {
                logger.info("No data point found for key ${it.key}. Remove it from template.")
                replaceFieldInTemplate(frameworkTemplate, it.key, "", getJsonNodeFromString("null"))
            }
        }
        logger.info("Assembled data set from data points:")
        logger.info(frameworkTemplate.toPrettyString())

        return frameworkTemplate.toString()
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
            if (jsonNode.has("id") && jsonNode.has("ref")) {
                results[fieldName] = jsonNode.get("id").asText()
            } else {
                val fields = jsonNode.fields()
                while (fields.hasNext()) {
                    val jsonField = fields.next()
                    val nextFieldName = if (fieldName.isEmpty()) jsonField.key else "$fieldName.${jsonField.key}"
                    results += extractDataPointsFromFrameworkTemplate(jsonField.value, nextFieldName)
                }
            }
        }
        return results
    }

    fun storeDataPoint(
        uploadedData: UploadableDataPoint,
        uploaderUserId: String,
        bypassQa: Boolean,
        correlationId: String,
    ): String {
        logger.info("Executing check for '${uploadedData.datapointSpecification}' data point (correlation ID: $correlationId).")
        validateDataPoint(uploadedData.datapointSpecification, uploadedData.data, correlationId)
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
        dataManager.storeDataSetInTemporaryStoreAndSendMessage(
            dataId.toString(),
            storableDataSet,
            bypassQa,
            correlationId,
        )

        return dataId.toString()
    }

    private fun validateDataPoint(
        dataPoint: String,
        data: String,
        correlationId: String,
    ) {
        logger.info("Validating data point $dataPoint (correlation ID: $correlationId)")
        val dataPointType: String

        try {
            dataPointType = specificationManager.getDataPointSpecification(dataPoint).validatedBy.id
        } catch (clientException: ClientException) {
            logger.error(
                "A ClientException is thrown while retrieving the validating class for the data point." +
                    "(correlation ID: $correlationId)",
            )
            val responseBody = (clientException.response as ClientError<*>).body.toString()
            exceptionForwarder.catchDataPointValidationClassNotFoundClientException(
                response = responseBody,
                statusCode = clientException.statusCode,
                throwable = clientException,
            )
            throw clientException
        }

        val validationClass = specificationManager.getDataPointTypeSpecification(dataPointType).validatedBy
        validateConsistency(data, validationClass, correlationId)
    }

    private fun validateConsistency(
        jsonData: String,
        className: String,
        correlationId: String,
    ) {
        val classForValidation = Class.forName(className).kotlin.java
        val validator = Validation.buildDefaultValidatorFactory().validator
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        val dataPointObject = objectMapper.readValue(jsonData, classForValidation)
        val violations = validator.validate(dataPointObject)
        if (violations.isNotEmpty()) {
            logger.error("Validation failed when casting $jsonData into $className (correlation ID: $correlationId)")
            var errorMessage = "Validation failed for data point of type $className: "
            violations.forEach {
                logger.error(it.message)
                errorMessage += (it.message)
            }

            throw IllegalArgumentException(errorMessage)
        }
    }
}
