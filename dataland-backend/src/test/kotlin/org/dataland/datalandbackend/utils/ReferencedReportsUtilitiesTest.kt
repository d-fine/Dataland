package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.utils.JsonTestUtils.testObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class ReferencedReportsUtilitiesTest {
    private val currencyDataPoint = "./json/validation/currencyDataPoint.json"
    private val currencyDataPointWithExtendedDocumentReference =
        "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"
    private val frameworkTemplate = "./json/frameworkTemplate/template.json"
    private val frameworkWithReferencedReports = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val frameworkWithoutReferencedReports = "./json/frameworkTemplate/frameworkWithoutReferencedReports.json"
    private val frameworkWithDataSource = "./json/frameworkTemplate/frameworkWithDataSources.json"
    private val expectedFrameworkWithDataSource = "./json/frameworkTemplate/expectedFrameworkWithDataSources.json"
    private val templateWithReferencedReports = "./json/frameworkTemplate/templateWithReferencedReports.json"
    private val frameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"

    private val testDate = "2023-11-04"
    private val anotherTestDate = "2023-05-03"

    private val referencedReportsUtilities = ReferencedReportsUtilities(testObjectMapper)

    private fun readDataContent(resourceFile: String): Map<String, JsonSpecificationLeaf> {
        val schema =
            testObjectMapper
                .readTree(TestResourceFileReader.getKotlinObject<FrameworkSpecification>(frameworkSpecification).schema)
        referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(schema, "general.general.referencedReports")
        return JsonSpecificationUtils.dehydrateJsonSpecification(
            schema as ObjectNode,
            TestResourceFileReader.getJsonNode(resourceFile) as ObjectNode,
        )
    }

    @Test
    fun `check that extraction of the referenced report works as expected`() {
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReference)
        val dataSource = testObjectMapper.readValue(dataPoint, CurrencyDataPoint::class.java).dataSource
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
        val result =
            referencedReportsUtilities
                .validateReferencedReportConsistency(dataContent[ReferencedReportsUtilities.REFERENCED_REPORTS_ID])
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that validateReferencedReportConsistency returns report map when input is valid`() {
        val dataContent = readDataContent(frameworkWithReferencedReports)
        val result =
            referencedReportsUtilities
                .validateReferencedReportConsistency(dataContent[ReferencedReportsUtilities.REFERENCED_REPORTS_ID])
        assertEquals(2, result.size)
        assertEquals("70a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63", result["AnnualReport"]?.fileReference)
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
                content = testObjectMapper.readTree(duplicateRefReferenceReport),
            )
        assertThrows<InvalidInputApiException> {
            referencedReportsUtilities.validateReferencedReportConsistency(jsonSpecificationNode)
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
    fun `check that inconsistent publication Dates references cause an error`() {
        val referencedReports =
            mapOf(
                "fileName" to CompanyReport(fileReference = "fileReference", publicationDate = LocalDate.parse(testDate)),
            )
        val reportFromDatapoint =
            CompanyReport(
                fileReference = "fileReference",
                publicationDate = LocalDate.parse(anotherTestDate),
            )
        assertThrows<InvalidInputApiException> {
            referencedReportsUtilities.validateReportConsistencyWithGlobalList(reportFromDatapoint, referencedReports)
        }
    }

    @Test
    fun `check that updating a single data point with a publication date works as expected`() {
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReference)

        val dataSource = testObjectMapper.readValue(dataPoint, CurrencyDataPoint::class.java).dataSource
        val contentNode = testObjectMapper.readTree(dataPoint)

        requireNotNull(dataSource) { "Data point does not contain a proper data source" }

        val fileReferenceToPublicationDateMapping = mapOf(dataSource.fileReference to LocalDate.parse(testDate))

        referencedReportsUtilities.updatePublicationDateInJsonNode(
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
                testObjectMapper.readValue(it.toString(), ExtendedDocumentReference::class.java)
            }
        assertEquals(expected, actual)
    }

    @Test
    fun `check that updating a framework with a publication date works as expected`() {
        val frameworkContent = TestResourceFileReader.getJsonNode(frameworkWithDataSource)
        val expected = TestResourceFileReader.getJsonNode(expectedFrameworkWithDataSource)

        val fileReferenceToPublicationDateMapping =
            mapOf(
                "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2022-12-05"),
                "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse(testDate),
            )

        referencedReportsUtilities.updatePublicationDateInJsonNode(
            frameworkContent,
            fileReferenceToPublicationDateMapping,
            "",
        )

        assertEquals(expected, frameworkContent)
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
}
