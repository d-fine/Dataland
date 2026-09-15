package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class ReferencedReportsUtilitiesTest {
    private val currencyDataPoint = "./json/validation/currencyDataPoint.json"
    private val currencyDataPointWithExtendedDocumentReferenceAndInferableFields =
        "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReferenceAndInferableFields.json"
    private val frameworkTemplate = "./json/frameworkTemplate/template.json"
    private val frameworkWithReferencedReports = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val frameworkWithoutReferencedReports = "./json/frameworkTemplate/frameworkWithoutReferencedReports.json"
    private val frameworkWithDataSource = "./json/frameworkTemplate/frameworkWithDataSources.json"
    private val templateWithReferencedReports = "./json/frameworkTemplate/templateWithReferencedReports.json"
    private val frameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val dataPointWithMultipleSources = "./json/frameworkTemplate/dataPointWithMultipleSources.json"

    private val testDate = "2023-11-04"
    private val anotherTestDate = "2023-05-03"

    private val referencedReportsUtilities = ReferencedReportsUtilities()

    private fun readDataContent(resourceFile: String): Map<String, JsonSpecificationLeaf> {
        val schema =
            defaultObjectMapper
                .readTree(TestResourceFileReader.getKotlinObject<FrameworkSpecification>(frameworkSpecification).schema)
        referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(schema, "general.general.referencedReports")
        return JsonSpecificationUtils.dehydrateJsonSpecification(
            schema as ObjectNode,
            TestResourceFileReader.getJsonNode(resourceFile) as ObjectNode,
        )
    }

    @Test
    fun `check that extraction of the referenced report works as expected`() {
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReferenceAndInferableFields)
        val dataSource = defaultObjectMapper.readValue(dataPoint, ExtendedCurrencyDataPoint::class.java).dataSource
        val expectedCompanyReport =
            CompanyReport(
                fileReference = dataSource?.fileReference ?: "dummy",
                fileName = dataSource?.fileName,
                publicationDate = LocalDate.parse(testDate),
            )
        val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(dataPoint)
        assertEquals(expectedCompanyReport, companyReport)
    }

    @Test
    fun `check that a data point without data source yields null`() {
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPoint)
        val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(dataPoint)
        assertEquals(null, companyReport)
    }

    @Test
    fun `check that validateReferencedReportConsistency returns empty map when input is null`() {
        val dataContent = readDataContent(frameworkWithoutReferencedReports)
        val referencedReports =
            referencedReportsUtilities
                .parseReferencedReportsFromJsonLeaf(dataContent[ReferencedReportsUtilities.REFERENCED_REPORTS_ID])
        referencedReportsUtilities
            .validateReferencedReportConsistency(referencedReports)
        assertTrue(referencedReports.isEmpty())
    }

    @Test
    fun `check that validateReferencedReportConsistency returns report map when input is valid`() {
        val dataContent = readDataContent(frameworkWithReferencedReports)
        val referencedReports =
            referencedReportsUtilities
                .parseReferencedReportsFromJsonLeaf(dataContent[ReferencedReportsUtilities.REFERENCED_REPORTS_ID])
        referencedReportsUtilities
            .validateReferencedReportConsistency(referencedReports)
        assertEquals(2, referencedReports.size)
        assertEquals("70a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63", referencedReports["AnnualReport"]?.fileReference)
    }

    @Test
    fun `check that validateReferencedReportConsistency returns an error when duplicate file is uploaded`() {
        val duplicateRefReferenceReport =
            """
            { "a": { "fileReference": "ref1" }, "b": { "fileReference": "ref1" } }
            """.trimIndent()
        val jsonSpecificationNode =
            JsonSpecificationLeaf(
                dataPointId = "id",
                jsonPath = "path",
                content = defaultObjectMapper.readTree(duplicateRefReferenceReport),
            )
        assertThrows<InvalidInputApiException> {
            val referencedReports = referencedReportsUtilities.parseReferencedReportsFromJsonLeaf(jsonSpecificationNode)
            referencedReportsUtilities.validateReferencedReportConsistency(referencedReports)
        }
    }

    @Test
    fun `check that an unlisted datasource throws an error`() {
        val referencedReports = mapOf<String, CompanyReport>()
        val reportFromDatapoint =
            CompanyReport(
                fileReference = "fileReference",
                fileName = "fileName",
            )
        assertThrows<InvalidInputApiException> {
            referencedReportsUtilities.validateReportConsistencyWithGlobalList(reportFromDatapoint, referencedReports)
        }
    }

    @Test
    fun `check that validateReportConsistencyWithGlobalList no longer compares fileName or publicationDate`() {
        val referencedReports =
            mapOf(
                "fileName" to CompanyReport(fileReference = "fileReference", publicationDate = LocalDate.parse(testDate)),
            )
        val reportFromDatapoint =
            CompanyReport(
                fileReference = "fileReference",
                publicationDate = LocalDate.parse(anotherTestDate),
            )
        assertDoesNotThrow {
            referencedReportsUtilities.validateReportConsistencyWithGlobalList(reportFromDatapoint, referencedReports)
        }
    }

    @Test
    fun `check that validateReferencedReportConsistency rejects a populated fileName`() {
        val referencedReports =
            mapOf(
                "AnnualReport" to CompanyReport(fileReference = "ref1", fileName = "AnnualReport"),
            )
        assertThrows<InvalidInputApiException> {
            referencedReportsUtilities.validateReferencedReportConsistency(referencedReports)
        }
    }

    @Test
    fun `check that validateReferencedReportConsistency rejects a populated publicationDate`() {
        val referencedReports =
            mapOf(
                "AnnualReport" to CompanyReport(fileReference = "ref1", publicationDate = LocalDate.parse(testDate)),
            )
        assertThrows<InvalidInputApiException> {
            referencedReportsUtilities.validateReferencedReportConsistency(referencedReports)
        }
    }

    @Test
    fun `check that validateDataSourcesDoNotContainInferableFields returns no violations for a clean data source`() {
        val cleanDataPoint =
            """
            { "value": "100", "dataSource": { "page": "6", "tagName": "content", "fileReference": "ref1" } }
            """.trimIndent()
        val violations =
            referencedReportsUtilities.validateDataSourcesDoNotContainInferableFields(
                defaultObjectMapper.readTree(cleanDataPoint),
                "root",
            )
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `check that validateDataSourcesDoNotContainInferableFields detects fileName and publicationDate`() {
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReferenceAndInferableFields)
        val violations =
            referencedReportsUtilities.validateDataSourcesDoNotContainInferableFields(
                defaultObjectMapper.readTree(dataPoint),
                "root",
            )
        assertEquals(2, violations.size)
        assertTrue(violations.any { it.contains("fileName") })
        assertTrue(violations.any { it.contains("publicationDate") })
    }

    @Test
    fun `check that validateDataSourcesDoNotContainInferableFields aggregates violations across a whole framework document`() {
        val frameworkContent = TestResourceFileReader.getJsonNode(frameworkWithDataSource)
        val violations =
            referencedReportsUtilities.validateDataSourcesDoNotContainInferableFields(frameworkContent, "root")
        // The fixture contains two data sources, each with a populated fileName and no populated publicationDate.
        assertEquals(2, violations.size)
        assertTrue(violations.all { it.contains("fileName") })
    }

    @Test
    fun `check that validateDataSourcesDoNotContainInferableFields does not affect nodes not named dataSource`() {
        val nodeNotNamedDataSource =
            """
            { "notADataSource": { "fileReference": "ref1", "fileName": "SomeFile", "publicationDate": "2023-11-04" } }
            """.trimIndent()
        val violations =
            referencedReportsUtilities.validateDataSourcesDoNotContainInferableFields(
                defaultObjectMapper.readTree(nodeNotNamedDataSource),
                "notADataSource",
            )
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `check that validateDataSourcesDoNotContainInferableFields detects violations in nested compound data points`() {
        val nestedContent =
            """
            {
              "absoluteShare": {
                "value": "100",
                "dataSource": { "page": "6", "tagName": "content", "fileReference": "ref1", "fileName": "File1" }
              },
              "relativeShareInPercent": {
                "value": "50",
                "dataSource": {
                  "page": "7", "tagName": "content", "fileReference": "ref2", "publicationDate": "2023-11-04"
                }
              }
            }
            """.trimIndent()
        val violations =
            referencedReportsUtilities.validateDataSourcesDoNotContainInferableFields(
                defaultObjectMapper.readTree(nestedContent),
                "root",
            )
        assertEquals(2, violations.size)
    }

    @Test
    fun `check that the referenced reports are correctly inserted into the framework template`() {
        val testNode = TestResourceFileReader.getJsonNode(frameworkTemplate)
        val targetPath = "category.subcategory.referencedReports"

        referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(testNode, targetPath)
        val expected = TestResourceFileReader.getJsonNode(templateWithReferencedReports)
        assertEquals(testNode, expected)
    }

    @Test
    fun `check that an empty referenced reports path is not inserted into the json node`() {
        val testNode = TestResourceFileReader.getJsonNode(frameworkTemplate)
        referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(testNode, null)
        assertEquals(testNode, TestResourceFileReader.getJsonNode(frameworkTemplate))
    }

    @Test
    fun `check that parsing a nested object returns the expected reports`() {
        val testContent = TestResourceFileReader.getJsonString(dataPointWithMultipleSources)
        val expectedReports =
            listOf(
                CompanyReport(
                    fileName = "SubBranch1",
                    fileReference = "1",
                ),
                CompanyReport(
                    fileName = "SubBranch2",
                    fileReference = "2",
                    publicationDate = LocalDate.parse(testDate),
                ),
                CompanyReport(
                    fileName = "Branch2",
                    fileReference = "3",
                    publicationDate = LocalDate.parse(anotherTestDate),
                ),
            )
        val actualReports = mutableListOf<CompanyReport>()
        referencedReportsUtilities.getAllCompanyReportsFromDataSource(testContent, actualReports)
        assertEquals(expectedReports, actualReports)
    }
}
