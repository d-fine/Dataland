package org.dataland.e2etests.tests

import com.fasterxml.jackson.databind.ObjectMapper
import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.BaseDataPointYesNo
import org.dataland.datalandbackend.openApiClient.model.BaseDocumentReference
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.datalandbackend.openApiClient.model.YesNo
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.io.File
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

class DocumentDeletionControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerApiAccessor = DocumentControllerApiAccessor()
    private val documentControllerClient = documentControllerApiAccessor.documentControllerApi
    private val objectMapperForJsonAssertion = ObjectMapper()

    companion object {
        private const val PDF_HEADER = """%PDF-1.4
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj
2 0 obj
<<
/Type /Pages
/Kids [3 0 R]
/Count 1
>>
endobj
3 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 612 792]
/Contents 4 0 R
/Resources <<
/Font <<
/F1 <<
/Type /Font
/Subtype /Type1
/BaseFont /Helvetica
>>
>>
>>
>>
endobj"""
    }

    @Test
    fun `test that a document with no references can be deleted successfully`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that deleting a non existent document returns 404`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistentDocumentId = "nonExistentDocumentId"

        val exception = assertThrows<ClientException> { documentControllerClient.deleteDocument(nonExistentDocumentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that users without proper authorization cannot delete documents`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf(), TechnicalUser.Uploader)
        val documentId = uploadResponse.documentId

        for (role in arrayOf(TechnicalUser.Reader, TechnicalUser.Reviewer)) {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(role)
            val exception = assertThrows<ClientException> { documentControllerClient.deleteDocument(documentId) }
            assertEquals(403, exception.statusCode)

            documentControllerClient.checkDocument(documentId)
        }
    }

    @Test
    fun `test that admin can delete any document`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf(), TechnicalUser.Uploader)
        val documentId = uploadResponse.documentId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that uploader can delete their own document`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf(), TechnicalUser.Uploader)
        val documentId = uploadResponse.documentId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that document with LkSG dataset reference in Pending status cannot be deleted`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId
        awaitDocumentAvailable(documentId)

        val testCompanyInformation =
            apiAccessor.testDataProviderForLksgData
                .getCompanyInformationWithoutIdentifiers(1)
                .first()

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId

        val testData =
            apiAccessor.testDataProviderForLksgData
                .getTData(1)
                .first()
        val modifiedData = addDocumentReferenceToLksgDataset(testData, documentId)

        apiAccessor
            .lksgUploaderFunction(
                companyId,
                modifiedData,
                "2023",
                bypassQa = false,
            )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exception =
            assertThrows<ClientException> {
                documentControllerClient.deleteDocument(documentId)
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    @Test
    fun `test that document with LkSG dataset reference in Accepted status cannot be deleted`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId
        awaitDocumentAvailable(documentId)

        val testCompanyInformation =
            apiAccessor.testDataProviderForLksgData
                .getCompanyInformationWithoutIdentifiers(1)
                .first()

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId

        val testData =
            apiAccessor.testDataProviderForLksgData
                .getTData(1)
                .first()
        val modifiedData = addDocumentReferenceToLksgDataset(testData, documentId)

        val dataId =
            apiAccessor
                .lksgUploaderFunction(
                    companyId,
                    modifiedData,
                    "2023",
                    bypassQa = false,
                ).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, QaStatus.Accepted)
        awaitUntilQaStatusEquals(dataId, QaStatus.Accepted)

        val exception =
            assertThrows<ClientException> {
                documentControllerClient.deleteDocument(documentId)
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    @Test
    fun `test that document with LkSG dataset reference in Rejected status can be deleted`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId
        awaitDocumentAvailable(documentId)

        val testCompanyInformation =
            apiAccessor.testDataProviderForLksgData
                .getCompanyInformationWithoutIdentifiers(1)
                .first()

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId

        val testData =
            apiAccessor.testDataProviderForLksgData
                .getTData(1)
                .first()
        val modifiedData = addDocumentReferenceToLksgDataset(testData, documentId)

        val dataId =
            apiAccessor
                .lksgUploaderFunction(
                    companyId,
                    modifiedData,
                    "2023",
                    bypassQa = false,
                ).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, QaStatus.Rejected)
        awaitUntilQaStatusEquals(dataId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that document with data point reference without QA review cannot be deleted`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId

        val companyId = createCompany()

        val dataPointJson =
            """{"value": 0.5, "currency": "USD", "dataSource": { "fileReference": "$documentId" } }"""
        uploadDataPoint(companyId, dataPointJson, bypassQa = false)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exception =
            assertThrows<ClientException> {
                documentControllerClient.deleteDocument(documentId)
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    @Test
    fun `test that document with data point reference in Rejected status can be deleted`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId

        val companyId = createCompany()

        val dataPointJson =
            """{"value": 0.5, "currency": "USD", "dataSource": { "fileReference": "$documentId" } }"""
        val dataPointId = uploadDataPoint(companyId, dataPointJson, bypassQa = false).dataPointId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.qaServiceControllerApi.changeDataPointQaStatus(dataPointId, QaStatus.Rejected)
        awaitUntilDataPointQaStatusEquals(dataPointId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that document deletion nullifies file references and attachment in rejected LkSG datasets`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId
        awaitDocumentAvailable(documentId)

        val testCompanyInformation =
            apiAccessor.testDataProviderForLksgData
                .getCompanyInformationWithoutIdentifiers(1)
                .first()

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId

        val testData =
            apiAccessor.testDataProviderForLksgData
                .getTData(1)
                .first()
        val modifiedData = addDocumentReferenceToLksgDataset(testData, documentId)

        val dataId =
            apiAccessor
                .lksgUploaderFunction(
                    companyId,
                    modifiedData,
                    "2023",
                    bypassQa = false,
                ).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, QaStatus.Rejected)
        awaitUntilQaStatusEquals(dataId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)

        val retrievedDataset = Backend.lksgDataControllerApi.getCompanyAssociatedLksgData(dataId).data
        val riskManagementSystem = retrievedDataset.governance?.riskManagementOwnOperations?.riskManagementSystem
        assertNull(
            riskManagementSystem?.dataSource,
            "Document reference in riskManagementSystem dataSource should be null after document deletion",
        )
    }

    @Test
    fun `test that document deletion nullifies file references in rejected datapoints`() {
        val uploadResponse = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf())
        val documentId = uploadResponse.documentId

        val companyId = createCompany()

        val dataPointJson =
            """{"value": 0.5, "currency": "USD", "dataSource": { "fileReference": "$documentId", "fileName": "test.pdf" } }"""
        val dataPointId = uploadDataPoint(companyId, dataPointJson, bypassQa = false).dataPointId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.qaServiceControllerApi.changeDataPointQaStatus(dataPointId, QaStatus.Rejected)
        awaitUntilDataPointQaStatusEquals(dataPointId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)

        val retrievedDataPoint = Backend.dataPointControllerApi.getDataPoint(dataPointId).dataPoint
        val innerData = unwrapPossiblyEncodedJson(retrievedDataPoint, objectMapperForJsonAssertion)
        val dataSourceNode = innerData.get("dataSource")
        assertTrue(dataSourceNode == null || dataSourceNode.isNull, "Entire dataSource object should be null after document deletion")
    }

    private fun addDocumentReferenceToLksgDataset(
        dataset: LksgData,
        documentId: String,
    ): LksgData {
        val documentReference =
            BaseDocumentReference(
                fileReference = documentId,
                fileName = "TestDoc.pdf",
                publicationDate = LocalDate.now(),
            )

        val riskManagementSystemWithDoc =
            BaseDataPointYesNo(
                value = YesNo.Yes,
                dataSource = documentReference,
            )

        val updatedGovernance =
            dataset.governance?.copy(
                riskManagementOwnOperations =
                    dataset.governance?.riskManagementOwnOperations?.copy(
                        riskManagementSystem = riskManagementSystemWithDoc,
                    ),
            )

        return dataset.copy(governance = updatedGovernance)
    }

    private fun createCompany(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getCompanyInformationWithoutIdentifiers(1)
                .first()
        return Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId
    }

    private fun uploadDataPoint(
        companyId: String,
        dataPointJson: String,
        bypassQa: Boolean,
    ): DataPointMetaInformation {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPoint = dataPointJson,
                dataPointType = "extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption",
                companyId = companyId,
                reportingPeriod = "2022",
            )
        return Backend.dataPointControllerApi.postDataPoint(uploadedDataPoint, bypassQa)
    }

    private fun unwrapPossiblyEncodedJson(
        json: String,
        mapper: ObjectMapper,
    ): com.fasterxml.jackson.databind.JsonNode {
        var node = mapper.readTree(json)
        if (node.isTextual) node = mapper.readTree(node.asText())
        val dataField = node.get("data")
        return if (dataField != null && dataField.isTextual) mapper.readTree(dataField.asText()) else node
    }

    private fun awaitDocumentAvailable(documentId: String) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
            documentControllerClient.checkDocument(documentId)
        }
    }

    private fun awaitUntilQaStatusEquals(
        dataId: String,
        expectedStatus: QaStatus,
    ) {
        Awaitility.await().atMost(3000, TimeUnit.MILLISECONDS).pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
            val qaReview = apiAccessor.qaServiceControllerApi.getQaReviewResponseByDataId(UUID.fromString(dataId))
            assertEquals(expectedStatus, qaReview.qaStatus)
        }
    }

    private fun awaitUntilDataPointQaStatusEquals(
        dataPointId: String,
        expectedStatus: QaStatus,
    ) {
        Awaitility.await().atMost(3000, TimeUnit.MILLISECONDS).pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
            val qaReviews = apiAccessor.qaServiceControllerApi.getDataPointQaReviewInformationByDataId(dataPointId)
            val latestReview = qaReviews.firstOrNull()
            assertEquals(expectedStatus, latestReview?.qaStatus)
        }
    }

    private fun createUniquePdf(): File {
        val uniqueContent = "Test Document ${UUID.randomUUID()}"
        val tempFile = File.createTempFile("test-document-", ".pdf")
        tempFile.deleteOnExit()

        val pdfContent =
            """
$PDF_HEADER
4 0 obj
<<
/Length ${uniqueContent.length + 21}
>>
stream
BT
/F1 12 Tf
100 700 Td
($uniqueContent) Tj
ET
endstream
endobj
xref
0 5
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000115 00000 n
0000000317 00000 n
trailer
<<
/Size 5
/Root 1 0 R
>>
startxref
${420 + uniqueContent.length}
%%EOF
            """.trimIndent()

        tempFile.writeText(pdfContent)
        return tempFile
    }
}
