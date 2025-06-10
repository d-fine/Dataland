package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.DataPointBaseTypeSpecification
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class DataPointValidatorTest {
    private val objectMapper = JsonUtils.defaultObjectMapper
    private val specificationClient = mock<SpecificationControllerApi>()
    private val referencedReportsUtilities = mock<ReferencedReportsUtilities>()
    private val dataPointValidator =
        DataPointValidator(
            objectMapper, specificationClient, referencedReportsUtilities,
            Validation
                .buildDefaultValidatorFactory()
                .validator,
        )

    private val correlationId = "correlationId"
    private val validationClass = "org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint"

    private val currencyDataPoint = "./dataPointValidation/currencyDataPoint.json"
    private val invalidCurrencyDataPoint = "./dataPointValidation/invalidCurrencyDataPoint.json"
    private val currencyDataPointWithUnknownProperty = "./dataPointValidation/currencyDataPointWithUnknownProperty.json"
    private val currencyDataPointWithBrokenEnum = "./dataPointValidation/currencyDataPointWithBrokenEnum.json"
    private val activityDataPoint = "./dataPointValidation/activityDataPoint.json"

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

        val dataPoint =
            JsonSpecificationLeaf(
                dataPointId = dataPointId,
                jsonPath = "dummy",
                content = getJsonNode(currencyDataPoint),
            )

        val companyReport =
            CompanyReport(
                fileReference = "fileReference",
                fileName = "fileName",
                publicationDate = LocalDate.parse("2021-01-01"),
            )

        doReturn(
            mock<DataPointTypeSpecification> {
                on { dataPointBaseType } doReturn IdWithRef(id = dataPointBaseTypeId, ref = "dummy")
                on { dataPointType } doReturn IdWithRef(id = dataPointId, ref = "dummy")
            },
        ).whenever(specificationClient).getDataPointTypeSpecification(dataPointId)

        doReturn(
            mock<DataPointBaseTypeSpecification> {
                on { dataPointBaseType } doReturn IdWithRef(id = dataPointBaseTypeId, ref = "dummy")
                on { validatedBy } doReturn "org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint"
            },
        ).whenever(specificationClient).getDataPointBaseType(dataPointBaseTypeId)

        assertThrows<InvalidInputApiException> {
            dataPointValidator.validateDataset(mapOf(dataPointId to dataPoint), mapOf("report" to companyReport), correlationId)
        }
    }

    @Test
    fun `verify that an unknown constraint in the specification service causes an internal server exception`() {
        assertThrows<InternalServerErrorApiException> {
            dataPointValidator.validateConsistency(
                getJsonString(currencyDataPoint),
                validationClass,
                correlationId,
                listOf("between:0,100", "invalid constraint", "max:200"),
            )
        }
    }

    @Test
    fun `verify that data points with array are not seen as empty`() {
        val activityValidationClass =
            "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint<java.util." +
                "List<org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity>>"
        val dataPointId = "someAlignedActivityDataPoint"
        val dataPointBaseTypeId = "extendedEuTaxonomyAlignedActivitiesComponent"

        doReturn(
            DataPointTypeSpecification(
                dataPointBaseType = IdWithRef(id = dataPointBaseTypeId, ref = "dummy"),
                dataPointType = IdWithRef(id = dataPointId, ref = "dummy"),
                name = "dummy",
                businessDefinition = "dummy",
                usedBy = emptyList(),
            ),
        ).whenever(specificationClient).getDataPointTypeSpecification(dataPointId)

        doReturn(
            mock<DataPointBaseTypeSpecification> {
                on { validatedBy } doReturn activityValidationClass
            },
        ).whenever(specificationClient).getDataPointBaseType(dataPointBaseTypeId)

        val dataPoint =
            JsonSpecificationLeaf(
                dataPointId = dataPointId,
                jsonPath = "dummy",
                content = getJsonNode(activityDataPoint),
            )

        assertDoesNotThrow {
            dataPointValidator.validateDataset(mapOf(dataPointId to dataPoint), mapOf(), correlationId)
        }
    }
}
