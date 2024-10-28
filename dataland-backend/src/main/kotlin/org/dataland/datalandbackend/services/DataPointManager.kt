package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
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

    private fun constructDataPointUrl(dataPoint: String): String = "$specificationServiceBaseUrl/datapoints/$dataPoint.json"

    private fun constructDataTypeUrl(dataType: String): String = "$specificationServiceBaseUrl/datapointtypes/$dataType.json"

    private fun getDataType(dataPoint: String): String {
        val dataTypeUrl = getJsonNodeFromUrl(constructDataPointUrl(dataPoint)).get("dataType").asText()
        return dataTypeUrl.split("/").last().replace(".json", "")
    }

    /*fun doesDataPointDefinitionExist(dataPoint: String): Boolean =
        try {
            checkIfDataPointDefinitionExists(dataPoint)
            true
        } catch (e: IllegalArgumentException) {
            false
        }*/

    private fun checkIfDataPointDefinitionExists(dataPoint: String) {
        logger.info("Checking if data point definition exists for $dataPoint")
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
        logger.info("Executing check for '${uploadedData.dataPoint}' data point (correlation ID: $correlationId).")
        validateDataPoint(uploadedData.dataPoint, uploadedData.data)
        logger.info("Storing '${uploadedData.dataPoint}' data point.")
        val uploadTime = Instant.now().toEpochMilli()
        val storableDataSet =
            StorableDataSet(
                companyId = uploadedData.companyId.toString(),
                dataType = uploadedData.dataPoint,
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
