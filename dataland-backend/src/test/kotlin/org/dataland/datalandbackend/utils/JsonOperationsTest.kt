package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.utils.JsonOperations.getCompanyReportFromDataSource
import org.dataland.datalandbackend.utils.JsonOperations.getFileReferenceToPublicationDateMapping
import org.dataland.datalandbackend.utils.JsonOperations.insertReferencedReports
import org.dataland.datalandbackend.utils.JsonOperations.objectMapper
import org.dataland.datalandbackend.utils.JsonOperations.updatePublicationDateInJsonNode
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecificationDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class JsonOperationsTest {
    private val currencyDataPoint = "./json/validation/currencyDataPoint.json"
    private val currencyDataPointWithExtendedDocumentReference =
        "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"
    private val frameworkTemplate = "./json/frameworkTemplate/template.json"
    private val frameworkWithReferencedReports = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val frameworkWithoutReferencedReports = "./json/frameworkTemplate/frameworkWithoutReferencedReports.json"
    private val frameworkWithDataSource = "./json/frameworkTemplate/frameworkWithDataSources.json"
    private val expectedFrameworkWithDataSource = "./json/frameworkTemplate/expectedFrameworkWithDataSources.json"
    private val templateWithReferencedReports = "./json/frameworkTemplate/templateWithReferencedReports.json"
    private val templateWithNullReferencedReports = "./json/frameworkTemplate/templateWithNullReferencedReports.json"
    private val referencedReports = "./json/frameworkTemplate/referencedReports.json"
    private val frameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"

    private val testDate = "2023-11-04"
    private val anotherTestDate = "2023-05-03"
    private val referencedReportsPath = "general.general.referencedReports"

    private fun readDataContent(resourceFile: String): Map<String, JsonSpecificationLeaf> {
        val schema = TestResourceFileReader.getKotlinObject<FrameworkSpecificationDto>(frameworkSpecification).schema
        return JsonSpecificationUtils.dehydrateJsonSpecification(
            objectMapper.readTree(schema) as ObjectNode,
            TestResourceFileReader.getJsonNode(resourceFile) as ObjectNode,
            referencedReportsPath,
        )
    }

    @Test
    fun `check that extraction of the referenced report works as expected`() {
        val dataPointContent = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReference)
        val dataSource = objectMapper.readValue(dataPointContent, CurrencyDataPoint::class.java).dataSource
        val expectedCompanyReport =
            CompanyReport(
                fileReference = dataSource?.fileReference ?: "dummy",
                fileName = dataSource?.fileName,
                publicationDate = LocalDate.parse(testDate),
            )
        val companyReport = getCompanyReportFromDataSource(dataPointContent)
        assertEquals(expectedCompanyReport, companyReport)
    }

    @Test
    fun `check that a data point without data source yields null`() {
        val dataPointContent = TestResourceFileReader.getJsonString(currencyDataPoint)
        val companyReport = getCompanyReportFromDataSource(dataPointContent)
        assertEquals(null, companyReport)
    }

    @Test
    fun `check that the extracted mapping is as expected`() {
        val dataContent = readDataContent(frameworkWithReferencedReports)
        val extracted = getFileReferenceToPublicationDateMapping(dataContent[JsonSpecificationUtils.REFERENCED_REPORTS_ID])
        val expected =
            mapOf(
                "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse(testDate),
                "70a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse(anotherTestDate),
            )
        assertEquals(expected, extracted)
    }

    @Test
    fun `check that a framework without referenced reports yields an empty map`() {
        val dataContent = readDataContent(frameworkWithoutReferencedReports)
        val extracted = getFileReferenceToPublicationDateMapping(dataContent[JsonSpecificationUtils.REFERENCED_REPORTS_ID])
        assertTrue(extracted.isEmpty())
    }

    @Test
    fun `check that updating a single data point with a publication date works as expected`() {
        val dataPointContent = TestResourceFileReader.getJsonString(currencyDataPointWithExtendedDocumentReference)

        val dataSource = objectMapper.readValue(dataPointContent, CurrencyDataPoint::class.java).dataSource
        val contentNode = objectMapper.readTree(dataPointContent)

        requireNotNull(dataSource) { "Data point does not contain a proper data source" }

        val fileReferenceToPublicationDateMapping = mapOf(dataSource.fileReference to LocalDate.parse(testDate))

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
    fun `check that updating a framework with a publication date works as expected`() {
        val frameworkContent = TestResourceFileReader.getJsonNode(frameworkWithDataSource)
        val expected = TestResourceFileReader.getJsonNode(expectedFrameworkWithDataSource)

        val fileReferenceToPublicationDateMapping =
            mapOf(
                "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse("2022-12-05"),
                "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63" to LocalDate.parse(testDate),
            )

        updatePublicationDateInJsonNode(
            frameworkContent,
            fileReferenceToPublicationDateMapping,
            "",
        )

        assertEquals(expected, frameworkContent)
    }

    @Test
    fun `check that the referenced reports are correctly inserted into the framework template`() {
        val frameworkTemplate = TestResourceFileReader.getJsonNode(frameworkTemplate)
        val targetPath = "category.subcategory.referencedReports"
        val referencedReports = TestResourceFileReader.getKotlinObject<Map<String, CompanyReport>>(referencedReports)

        insertReferencedReports(frameworkTemplate, targetPath, referencedReports)
        val expected = TestResourceFileReader.getJsonNode(templateWithReferencedReports)
        assertEquals(frameworkTemplate, expected)
    }

    @Test
    fun `check that empty referenced reports are inserted as null into the framework template`() {
        val frameworkTemplate = TestResourceFileReader.getJsonNode(frameworkTemplate)
        val targetPath = "category.subcategory.referencedReports"
        val referencedReports = emptyMap<String, CompanyReport>()

        insertReferencedReports(frameworkTemplate, targetPath, referencedReports)
        val expected = TestResourceFileReader.getJsonNode(templateWithNullReferencedReports)
        assertEquals(frameworkTemplate, expected)
    }

    @Test
    fun `check that inserting a path that does not exist throws an exception `() {
        val frameworkTemplate = TestResourceFileReader.getJsonNode(frameworkTemplate)
        val targetPath = "does.not.exist"
        val referencedReports = TestResourceFileReader.getKotlinObject<Map<String, CompanyReport>>(referencedReports)

        assertThrows<IllegalArgumentException> { insertReferencedReports(frameworkTemplate, targetPath, referencedReports) }
    }
}
