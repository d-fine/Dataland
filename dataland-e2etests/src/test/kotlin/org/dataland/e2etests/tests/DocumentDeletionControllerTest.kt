package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.BaseDataPointYesNo
import org.dataland.datalandbackend.openApiClient.model.BaseDocumentReference
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.datalandbackend.openApiClient.model.YesNo
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setupAuth() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
    }

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
    fun `test that deleting a non existent document returns 404`() {
        val nonExistentDocumentId = "nonExistentDocumentId"

        val exception = assertThrows<ClientException> { documentControllerClient.deleteDocument(nonExistentDocumentId) }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `test that users without proper authorization cannot delete documents`() {
        val documentId = uploadDocumentAndGetId()

        for (role in arrayOf(TechnicalUser.Reader, TechnicalUser.Reviewer)) {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(role)
            val exception = assertThrows<ClientException> { documentControllerClient.deleteDocument(documentId) }
            assertEquals(403, exception.statusCode)

            assertDocumentExists(documentId)
        }
    }

    @Test
    fun `test that admin can delete any document`() {
        val documentId = uploadDocumentAndGetId()

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            documentControllerClient.deleteDocument(documentId)
        }

        assertDocumentDeleted(documentId)
    }

    @Test
    fun `test that uploader can delete their own document`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            val documentId = uploadDocumentAndGetId()

            documentControllerClient.deleteDocument(documentId)

            assertDocumentDeleted(documentId)
        }
    }

    @Test
    fun `test that document with LkSG dataset reference in Pending status cannot be deleted`() {
        val documentId = uploadDocumentAndGetId()

        uploadLksgDatasetWithDocumentReference(documentId, qaStatus = null)

        val exception =
            assertThrows<ClientException> {
                documentControllerClient.deleteDocument(documentId)
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    @Test
    fun `test that document with LkSG dataset reference in Accepted status cannot be deleted`() {
        val documentId = uploadDocumentAndGetId()

        uploadLksgDatasetWithDocumentReference(documentId, QaStatus.Accepted)

        val exception =
            assertThrows<ClientException> {
                documentControllerClient.deleteDocument(documentId)
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    @Test
    fun `test that document with LkSG dataset reference in Rejected status can be deleted`() {
        val documentId = uploadDocumentAndGetId()

        uploadLksgDatasetWithDocumentReference(documentId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        assertDocumentDeleted(documentId)
    }

    @Test
    fun `test that document with data point reference in Rejected status can be deleted`() {
        val documentId = uploadDocumentAndGetId()

        uploadAndRejectDataPointWithDocumentReference(documentId)

        documentControllerClient.deleteDocument(documentId)

        assertDocumentDeleted(documentId)
    }

    @Test
    fun `test that document deletion nullifies file references in rejected LkSG datasets`() {
        val documentId = uploadDocumentAndGetId()

        val dataId = uploadLksgDatasetWithDocumentReference(documentId, QaStatus.Rejected)
        awaitUntilQaStatusEquals(dataId, QaStatus.Rejected)

        documentControllerClient.deleteDocument(documentId)

        assertDocumentDeleted(documentId)

        val retrievedDataset = Backend.lksgDataControllerApi.getCompanyAssociatedLksgData(dataId).data
        val riskManagementSystem = retrievedDataset.governance?.riskManagementOwnOperations?.riskManagementSystem
        assertNull(
            riskManagementSystem?.dataSource,
            "Document reference in riskManagementSystem dataSource should be null after document deletion",
        )
    }

    @Test
    fun `test that document deletion nullifies file references in rejected datapoints`() {
        val documentId = uploadDocumentAndGetId()

        val dataPointId = uploadAndRejectDataPointWithDocumentReference(documentId)

        documentControllerClient.deleteDocument(documentId)

        assertDocumentDeleted(documentId)

        val retrievedDataPoint = Backend.dataPointControllerApi.getDataPoint(dataPointId).dataPoint
        val dataPointContent = unwrapEncodedJson(retrievedDataPoint)
        val dataSourceNode = dataPointContent.get("dataSource")
        assertTrue(
            dataSourceNode == null || dataSourceNode.isNull,
            "Entire dataSource object should be null after document deletion",
        )
    }

    private fun uploadDocumentAndGetId(): String {
        val documentId = documentControllerApiAccessor.uploadDocumentAsUser(createUniquePdf()).documentId
        awaitDocumentAvailable(documentId)
        return documentId
    }

    private fun assertDocumentDeleted(documentId: String) {
        val exception = assertThrows<ClientException> { documentControllerClient.checkDocument(documentId) }
        assertEquals(404, exception.statusCode)
    }

    private fun assertDocumentExists(documentId: String) {
        documentControllerClient.checkDocument(documentId)
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

    private fun createCompany(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val testCompanyInformation =
                apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                    .getCompanyInformationWithoutIdentifiers(1)
                    .first()
            Backend.companyDataControllerApi.postCompany(testCompanyInformation).companyId
        }

    private fun uploadDataPoint(
        companyId: String,
        dataPointJson: String,
    ): DataPointMetaInformation {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPoint = dataPointJson,
                dataPointType = "extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption",
                companyId = companyId,
                reportingPeriod = "2022",
            )
        return Backend.dataPointControllerApi.postDataPoint(uploadedDataPoint, false)
    }

    private fun uploadLksgDatasetWithDocumentReference(
        documentId: String,
        qaStatus: QaStatus? = null,
    ): String {
        val companyId = createCompany()
        val testData =
            apiAccessor.testDataProviderForLksgData
                .getTData(1)
                .first()

        val testDataWithDocumentReference = addDocumentReferenceToLksgDataset(testData, documentId)

        val dataId =
            apiAccessor
                .lksgUploaderFunction(
                    companyId,
                    testDataWithDocumentReference,
                    "2023",
                    false,
                ).dataId

        qaStatus?.let {
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                Awaitility
                    .await()
                    .atMost(10000, TimeUnit.MILLISECONDS)
                    .pollInterval(500, TimeUnit.MILLISECONDS)
                    .ignoreExceptions()
                    .untilAsserted {
                        apiAccessor.qaServiceControllerApi.getQaReviewResponseByDataId(UUID.fromString(dataId))
                    }
                apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, it)
                awaitUntilQaStatusEquals(dataId, it)
            }
        }

        return dataId
    }

    private fun uploadAndRejectDataPointWithDocumentReference(documentId: String): String {
        val companyId = createCompany()
        val dataPointJson =
            """{"value": 0.5, "currency": "USD", "dataSource": { "fileReference": "$documentId" } }"""

        val dataPointId = uploadDataPoint(companyId, dataPointJson).dataPointId

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            Awaitility
                .await()
                .atMost(10000, TimeUnit.MILLISECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted {
                    apiAccessor.qaServiceControllerApi.getDataPointQaReviewInformationByDataId(dataPointId)
                }
            apiAccessor.qaServiceControllerApi.changeDataPointQaStatus(dataPointId, QaStatus.Rejected)
            awaitUntilDataPointQaStatusEquals(dataPointId, QaStatus.Rejected)
        }

        return dataPointId
    }

    private fun unwrapEncodedJson(json: String): com.fasterxml.jackson.databind.JsonNode {
        var node = defaultObjectMapper.readTree(json)
        if (node.isTextual) node = defaultObjectMapper.readTree(node.asText())
        val dataField = node.get("data")
        return if (dataField != null && dataField.isTextual) defaultObjectMapper.readTree(dataField.asText()) else node
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
        Awaitility
            .await()
            .atMost(3000, TimeUnit.MILLISECONDS)
            .pollInterval(200, TimeUnit.MILLISECONDS)
            .ignoreExceptions()
            .untilAsserted {
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
