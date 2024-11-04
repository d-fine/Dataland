package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.utils.JsonOperations.extractDataPointsFromFrameworkTemplate
import org.dataland.datalandbackend.utils.JsonOperations.getCompanyReportFromDataSource
import org.dataland.datalandbackend.utils.JsonOperations.replaceFieldInTemplate
import org.dataland.datalandbackend.utils.JsonOperations.validateConsistency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class JsonOperationsTest {
    private val correlationId = "correlationId"
    private val validationClass = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint"

    private val currencyDataPoint = "./json/validation/currencyDataPoint.json"
    private val currencyDataPointWithExtendedDocumentReference =
        "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"
    private val invalidCurrencyDataPoint = "./json/validation/invalidCurrencyDataPoint.json"
    private val currencyDataPointWithUnknownProperty = "./json/validation/currencyDataPointWithUnknownProperty.json"
    private val frameworkTemplate = "./json/frameworkTemplate/template.json"
    private val frameworkTemplateAfterReplacement = "./json/frameworkTemplate/templateAfterReplacement.json"
    private val replacementValue = "./json/frameworkTemplate/replacementValue.json"

    private fun getJsonString(resourceFile: String): String =
        ObjectMapper()
            .readTree(this.javaClass.classLoader.getResourceAsStream(resourceFile))
            .toString()

    private fun getJsonNode(resourceFile: String): JsonNode =
        ObjectMapper()
            .readTree(this.javaClass.classLoader.getResourceAsStream(resourceFile))

    @Test
    fun `Check that a valid input passes the validation`() {
        assertDoesNotThrow { validateConsistency(getJsonString(currencyDataPoint), validationClass, correlationId) }
    }

    @Test
    fun `Check that unrecognized properties are rejected`() {
        assertThrows<UnrecognizedPropertyException> {
            validateConsistency(getJsonString(currencyDataPointWithUnknownProperty), validationClass, correlationId)
        }
    }

    @Test
    fun `Check that invalid inputs are rejected`() {
        assertThrows<IllegalArgumentException> {
            validateConsistency(getJsonString(invalidCurrencyDataPoint), validationClass, correlationId)
        }
    }

    @Test
    fun `Check that invalid classes are rejected`() {
        val className = "org.dataland.datalandbackend.model.datapoints.standard.DummyDataPoint"
        assertThrows<ClassNotFoundException> { validateConsistency("{}", className, correlationId) }
    }

    @Test
    fun `Check that parsing of a framework template yields the expected results`() {
        val frameworkTemplate = getJsonNode(frameworkTemplate)
        val expectedResults =
            mapOf(
                "category.subcategory.field" to "dataPoint",
                "anotherCategory.field2" to "anotherDataPoint",
                "anotherCategory.field3" to "yetAnotherDataPoint",
            )

        val results = extractDataPointsFromFrameworkTemplate(frameworkTemplate, "")
        assertEquals(expectedResults, results)
    }

    @Test
    fun `Check that replacement of a single data point yields the expected result`() {
        val frameworkTemplate = getJsonNode(frameworkTemplate)
        val replacementValue = getJsonNode(replacementValue)
        val expectedTemplate = getJsonNode(frameworkTemplateAfterReplacement)

        val fieldName = "category.subcategory.field"
        replaceFieldInTemplate(frameworkTemplate, fieldName, "", replacementValue)
        assertEquals(expectedTemplate, frameworkTemplate)
    }

    @Test
    fun `Check that extraction of the referenced report works as expected`() {
        val dataPointContent = getJsonString(currencyDataPointWithExtendedDocumentReference)
        val dataSource = jacksonObjectMapper().readValue(dataPointContent, CurrencyDataPoint::class.java).dataSource
        val expectedCompanyReport =
            CompanyReport(
                fileReference = dataSource?.fileReference ?: "dummy",
                fileName = dataSource?.fileName,
                publicationDate = null,
            )
        val companyReport = getCompanyReportFromDataSource(dataPointContent)
        assertEquals(expectedCompanyReport, companyReport)
    }

    @Test
    fun `Check that a data point without data source yields null`() {
        val dataPointContent = getJsonString(currencyDataPoint)
        val companyReport = getCompanyReportFromDataSource(dataPointContent)
        assertEquals(null, companyReport)
    }
}
