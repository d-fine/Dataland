package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.datapoints.StorableDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
*/
@Component("DataPointManager")
class DataPointManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Value("\${dataland.specification-service.base-url}")
    private val specificationServiceBaseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()
    private val dataPointInMemoryStorage = ConcurrentHashMap<UUID, String>()

    fun getJsonNodeFromUrl(url: String): JsonNode = ObjectMapper().readTree(URI(url).toURL())

    fun constructDataPointUrl(dataPoint: String): String = "$specificationServiceBaseUrl/datapoints/$dataPoint.json"

    fun constructDataTypeUrl(dataType: String): String = "$specificationServiceBaseUrl/datatypes/$dataType.json"

    fun getDataType(dataPoint: String): String {
        val dataTypeUrl = getJsonNodeFromUrl(constructDataPointUrl(dataPoint)).get("dataType").asText()
        return dataTypeUrl.split("/").last().replace(".json", "")
    }

    fun checkIfDataPointDefinitionExists(dataPoint: String) {
        logger.info("Checking if data point definition exists for $dataPoint")
        checkIfSpecificationExists(constructDataPointUrl(dataPoint))
    }

    fun checkIfSpecificationExists(specPath: String) {
        val url = URI(specPath).toURL()
        logger.info("Checking if specification exists at $url")
        try {
            getJsonNodeFromUrl(url.toString())
        } catch (e: FileNotFoundException) {
            logger.error("No specification for $specPath exists.")
            throw IllegalArgumentException("No specification for $specPath exists.")
        }
    }

    fun storeDataPoint(uploadedData: UploadableDataPoint): String {
        logger.info("Storing data point of types ${uploadedData.dataPoint}")
        checkIfDataPointDefinitionExists(uploadedData.dataPoint)
        val dataTypeUrl = constructDataTypeUrl(getDataType(uploadedData.dataPoint))
        checkIfSpecificationExists(dataTypeUrl)
        val validationClass = getJsonNodeFromUrl(dataTypeUrl).get("validatedBy").asText()
        validateDatapoint(uploadedData.data, validationClass)
        val dataId = UUID.randomUUID()
        dataPointInMemoryStorage[dataId] = objectMapper.writeValueAsString(uploadedData)
        return dataId.toString()
    }

    fun retrieveDataPoint(dataId: UUID): StorableDataPoint {
        logger.info("Retrieving data point with id $dataId")
        val storedData = dataPointInMemoryStorage[dataId] ?: throw IllegalArgumentException("Data point with id $dataId not found.")
        val uploadableDataPoint = objectMapper.readValue(storedData, UploadableDataPoint::class.java)

        return StorableDataPoint(
            dataPointId = dataId,
            dataPoint = uploadableDataPoint.dataPoint,
            companyId = uploadableDataPoint.companyId,
            reportingPeriod = uploadableDataPoint.reportingPeriod,
            data = uploadableDataPoint.data,
        )
    }

    fun validateDatapoint(
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
