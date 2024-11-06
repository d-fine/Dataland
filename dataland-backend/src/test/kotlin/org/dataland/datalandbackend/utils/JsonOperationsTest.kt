package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.utils.JsonOperations.extractDataPointsFromFrameworkTemplate
import org.dataland.datalandbackend.utils.JsonOperations.getCompanyReportFromDataSource
import org.dataland.datalandbackend.utils.JsonOperations.getFileReferenceToPublicationDateMapping
import org.dataland.datalandbackend.utils.JsonOperations.insertReferencedReports
import org.dataland.datalandbackend.utils.JsonOperations.objectMapper
import org.dataland.datalandbackend.utils.JsonOperations.replaceFieldInTemplate
import org.dataland.datalandbackend.utils.JsonOperations.updatePublicationDateInJsonNode
import org.dataland.datalandbackend.utils.JsonOperations.validateConsistency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.text.SimpleDateFormat
import java.time.LocalDate

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
    private val frameworkWithReferencedReports = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val frameworkWithoutReferencedReports = "./json/frameworkTemplate/frameworkWithoutReferencedReports.json"
    private val frameworkWithDataSource = "./json/frameworkTemplate/frameworkWithDataSources.json"
    private val expectedFrameworkWithDataSource = "./json/frameworkTemplate/expectedFrameworkWithDataSources.json"
    private val templateWithWithReferencedReports = "./json/frameworkTemplate/templateWithReferencedReports.json"
    private val referencedReports = "./json/frameworkTemplate/referencedReports.json"

    private fun getJsonString(resourceFile: String): String = getJsonNode(resourceFile).toString()

    private fun getJsonNode(resourceFile: String): JsonNode =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            )

    private inline fun <reified T> getKotlinObject(resourceFile: String): T =
        this.javaClass.classLoader
            .getResourceAsStream(resourceFile)
            ?.let { objectMapper.readValue<T>(it) } ?: throw IllegalArgumentException("Could not load the object")

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

    @Test
    fun `Check that the extracted mapping is as expected`() {
        val inputNode = getJsonNode(frameworkWithReferencedReports)
        val extracted = getFileReferenceToPublicationDateMapping(inputNode, "general.general.referencedReports")
        val expected =
            mapOf(
                "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2023-11-04"),
                "70a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2023-05-03"),
            )
        assertEquals(expected, extracted)
    }

    @Test
    fun `Check that a framework without referenced reports yields an empty map`() {
        val inputNode = getJsonNode(frameworkWithoutReferencedReports)
        val extracted = getFileReferenceToPublicationDateMapping(inputNode, "general.general.referencedReports")
        assertTrue(extracted.isEmpty())
    }

    @Test
    fun `Check that updating a single data point with a publication date works as expected`() {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dataPointContent = getJsonString(currencyDataPointWithExtendedDocumentReference)

        val dataSource = jacksonObjectMapper().readValue(dataPointContent, CurrencyDataPoint::class.java).dataSource
        val contentNode = jacksonObjectMapper().readTree(dataPointContent)

        if (dataSource?.fileReference == null) {
            throw IllegalArgumentException("Data point does not contain a proper data source")
        }

        val fileReferenceToPublicationDateMapping = mapOf(dataSource.fileReference to LocalDate.parse("2023-11-04"))

        updatePublicationDateInJsonNode(
            contentNode,
            fileReferenceToPublicationDateMapping,
            "dataSource",
        )

        val expected =
            ExtendedDocumentReference(
                fileReference = dataSource.fileReference,
                fileName = dataSource.fileName,
                page = dataSource.page,
                tagName = dataSource.tagName,
                publicationDate = fileReferenceToPublicationDateMapping[dataSource.fileReference],
            )
        val actual =
            contentNode.get("dataSource").let {
                objectMapper.readValue(it.toString(), ExtendedDocumentReference::class.java)
            }
        assertEquals(expected, actual)
    }

    @Test
    fun `Check that updating a framework with a publication date works as expected`() {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val frameworkContent = getJsonNode(frameworkWithDataSource)
        val expected = getJsonNode(expectedFrameworkWithDataSource)

        val fileReferenceToPublicationDateMapping =
            mapOf(
                "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2022-12-05"),
                "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2023-11-04"),
            )

        updatePublicationDateInJsonNode(
            frameworkContent,
            fileReferenceToPublicationDateMapping,
            "",
        )

        assertEquals(expected, frameworkContent)
    }

    @Test
    fun `Check that the referenced reports are correctly inserted into the framework template`() {
        val frameworkTemplate = getJsonNode(frameworkTemplate)
        val targetPath = "category.subcategory.referencedReports"
        val referencedReports = getKotlinObject<Map<String, CompanyReport>>(referencedReports)

        insertReferencedReports(frameworkTemplate, targetPath, referencedReports)
        val expected = getJsonNode(templateWithWithReferencedReports)
        assertEquals(frameworkTemplate, expected)
    }
}
