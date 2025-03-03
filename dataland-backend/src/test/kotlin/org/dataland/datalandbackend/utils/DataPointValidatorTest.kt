package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat
import java.time.LocalDate
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.specificationservice.openApiClient.model.DataPointBaseTypeSpecification
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef

class DataPointValidatorTest {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
    private val specificationClient = mock<SpecificationControllerApi>()
    private val referencedReportsUtilities = mock<ReferencedReportsUtilities>()
    private val dataPointValidator = DataPointValidator(objectMapper, specificationClient, referencedReportsUtilities)

    private val correlationId = "correlationId"
    private val validationClass = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint"

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
        assertThrows<ClassNotFoundException> { dataPointValidator.validateConsistency("{}", className, correlationId) }
    }

    @Test
    fun `retrieveDataPoint should throw InvalidInputApiException when dataPointType does not exist`() {
        val dataPoint = "dummy"
        val dataPointType = "non-existent-identifier"

        doThrow(ClientException("Data point identifier not found."))
            .whenever(specificationClient)
            .getDataPointTypeSpecification(dataPointType)

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

    @Test
    fun `check that unused referenced reports are rejected`() {
        val dataPointId = "someCurrencyDataPoint"
        val dataPointBaseTypeId = "extendedCurrencyDataPoint"

        val dataPoint = JsonSpecificationLeaf(
            dataPointId = dataPointId,
            jsonPath = "dummy",
            content = getJsonNode(currencyDataPoint)
        )

        val companyReport = CompanyReport(
            fileReference = "fileReference",
            fileName = "fileName",
            publicationDate = LocalDate.parse("2021-01-01"),
        )

        whenever(specificationClient.getDataPointTypeSpecification("dummy"))
            .thenReturn(DataPointTypeSpecification(
                dataPointType = IdWithRef(id = dataPointId, ref = "dummy"),
                name = "dummy",
                businessDefinition = "dummy",
                dataPointBaseType = IdWithRef(id = dataPointBaseTypeId, ref = "dummy"),
                usedBy = emptyList()
            ))

        whenever(specificationClient.getDataPointBaseType(dataPointBaseTypeId))
            .thenReturn(DataPointBaseTypeSpecification(
                dataPointBaseType = IdWithRef(id = dataPointBaseTypeId, ref = "dummy"),
                name = "dummy",
                businessDefinition = "dummy",
                validatedBy = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint",
                usedBy = emptyList(),
                example = "dummy"
            ))

        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateDataset(mapOf("dummy" to dataPoint), mapOf("report" to companyReport), correlationId)
        }
    }
}
