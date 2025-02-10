package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.text.SimpleDateFormat

class DataPointValidatorTest {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
    private val specificationClient = mock(SpecificationControllerApi::class.java)
    private val dataPointValidator =
        DataPointValidator(objectMapper, specificationClient, Validation.buildDefaultValidatorFactory().validator)

    private val correlationId = "correlationId"
    private val validationClass = "org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint"

    private val currencyDataPoint = "./dataPointValidation/currencyDataPoint.json"
    private val invalidCurrencyDataPoint = "./dataPointValidation/invalidCurrencyDataPoint.json"
    private val currencyDataPointWithUnknownProperty = "./dataPointValidation/currencyDataPointWithUnknownProperty.json"
    private val currencyDataPointWithBrokenEnum = "./dataPointValidation/currencyDataPointWithBrokenEnum.json"

    private fun getJsonString(resourceFile: String): String = getJsonNode(resourceFile).toString()

    private fun getJsonNode(resourceFile: String): JsonNode =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            )

    @Test
    fun `check that a valid input passes the validation`() {
        assertDoesNotThrow { dataPointValidator.validateConsistency(getJsonString(currencyDataPoint), validationClass, correlationId) }
    }

    @Test
    fun `check that unrecognized properties are rejected`() {
        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateConsistency(getJsonString(currencyDataPointWithUnknownProperty), validationClass, correlationId)
        }
    }

    @Test
    fun `check that invalid inputs are rejected`() {
        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateConsistency(getJsonString(invalidCurrencyDataPoint), validationClass, correlationId)
        }
    }

    @Test
    fun `check that invalid classes are rejected`() {
        val className = "org.dataland.datalandbackend.model.datapoints.standard.DummyDataPoint"
        assertThrows<IllegalArgumentException> { dataPointValidator.validateConsistency("{}", className, correlationId) }
    }

    @Test
    fun `retrieveDataPoint should throw InvalidInputApiException when dataPointType does not exist`() {
        val dataPoint = "dummy"
        val dataPointType = "non-existent-identifier"

        `when`(specificationClient.getDataPointTypeSpecification(dataPointType))
            .thenThrow(ClientException("Data point identifier not found."))

        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateDataPoint(dataPointType, dataPoint, correlationId)
        }
    }

    @Test
    fun `check that parsing a data point with a broken enum results in the expected exception`() {
        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateConsistency(getJsonString(currencyDataPointWithBrokenEnum), validationClass, correlationId)
        }
    }
}
