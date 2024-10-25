package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
*/
@Component("DataPointManager")
class DataPointManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()

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
            logger.error("Validation failed when casting $jsonData into $className")
            throw IllegalArgumentException("Validation failed for data point")
        }
    }
}
