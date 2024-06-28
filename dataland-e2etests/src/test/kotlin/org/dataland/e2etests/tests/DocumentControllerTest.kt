package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.model.DocumentUploadResponse
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.READER_USER_ID
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class DocumentControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerClient = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    private val pdfDocument = File("./public/test-report.pdf")
    private val xlsxDocument = File("./public/sample.xlsx")
    private val xlsDocument = File("./public/sample.xls")
    private val odsDocument = File("./public/sample.ods")
    private val docxDocument = File("./public/sample.docx")

    @Test
    fun `test that a dummy docx document can be uploaded and retrieved as pdf after successful QA`() {
        assertFalse(isByteArrayRepresentationOfPdf(docxDocument.readBytes()))
        val uploadResponse = uploadDocument(docxDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        val byteArrayOfDownload = downloadedFile.readBytes()
        validateResponseHeaders(uploadResponse, DocumentType.Pdf.mediaType, byteArrayOfDownload.size.toString())
        assertTrue(
            isByteArrayRepresentationOfPdf(
                byteArrayOfDownload,
            ),
            "downloaded document is a pdf document",
        )
    }

    @Test
    fun `test that a dummy ods document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(odsDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        val byteArrayOfOds = odsDocument.readBytes()
        validateResponseHeaders(uploadResponse, DocumentType.Ods.mediaType, byteArrayOfOds.size.toString())
        assertEquals(byteArrayOfOds.sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a dummy xlsx document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(xlsxDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        val byteArrayOfXlsx = xlsxDocument.readBytes()
        validateResponseHeaders(uploadResponse, DocumentType.Xlsx.mediaType, byteArrayOfXlsx.size.toString())
        assertEquals(byteArrayOfXlsx.sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a dummy xls document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(xlsDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        val byteArrayOfXls = xlsDocument.readBytes()
        validateResponseHeaders(uploadResponse, DocumentType.Xls.mediaType, byteArrayOfXls.size.toString())
        assertEquals(byteArrayOfXls.sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a dummy pdf document can be uploaded and retrieved after successful QA`() {
        val byteArrayOfPdf = pdfDocument.readBytes()
        val uploadResponse = uploadDocument(pdfDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        validateResponseHeaders(uploadResponse, DocumentType.Pdf.mediaType, byteArrayOfPdf.size.toString())
        assertEquals(byteArrayOfPdf.sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a non existing document cannot be found`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistentDocumentId = "nonExistentDocumentId"
        val exception = assertThrows<ClientException> {
            documentControllerClient.checkDocument(nonExistentDocumentId)
        }
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode)
    }

    @Test
    fun `test that users without keycloak-uploader-role can upload documents only if assigned company-owner-role`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyIdString = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val testCompanyId = UUID.fromString(testCompanyIdString)
        val dataReaderId = UUID.fromString(READER_USER_ID)

        removeAllCompanyOwnershipsFromUser(dataReaderId)

        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            testCompanyId,
            dataReaderId,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow { uploadDocument(pdfDocument, TechnicalUser.Reader) }

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        removeAllCompanyOwnershipsFromUser(dataReaderId)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val clientException = assertThrows<ClientException> {
            uploadDocument(pdfDocument, TechnicalUser.Reader)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    private fun removeAllCompanyOwnershipsFromUser(userId: UUID) {
        val companiesOwnedByUser = apiAccessor.companyRolesControllerApi.getCompanyRoleAssignments(
            CompanyRole.CompanyOwner,
            null,
            userId,
        )
        companiesOwnedByUser.forEach {
            apiAccessor.companyRolesControllerApi.removeCompanyRole(
                CompanyRole.CompanyOwner,
                UUID.fromString(it.companyId),
                userId,
            )
        }
    }

    /**
     * uploads a document
     * @param document document to upload
     * @returns the upload response
     */
    private fun uploadDocument(
        document: File,
        technicalUser: TechnicalUser = TechnicalUser.Uploader,
    ): DocumentUploadResponse {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val expectedHash = document.readBytes().sha256()
        val uploadResponse = documentControllerClient.postDocument(document)
        assertEquals(expectedHash, uploadResponse.documentId)
        documentControllerClient.checkDocument(uploadResponse.documentId)
        return uploadResponse
    }

    /**
     * Wait until QaStatus is accepted for uploaded document.
     *
     * @param uploadResponse the DocumentUploadResponse document's id for which an update of the QaStatus should be
     * checked and awaited
     * @returns the received file
     */
    private fun ensureQaCompleted(uploadResponse: DocumentUploadResponse): File {
        lateinit var downloadedFile: File
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until {
                try {
                    downloadedFile = documentControllerClient.getDocument(uploadResponse.documentId)
                    true
                } catch (e: ClientException) {
                    e.statusCode != HttpStatus.NOT_FOUND.value()
                }
            }
        return downloadedFile
    }

    /**
     * checks if response headers are correct
     * @param uploadResponse uploadResponse
     * @param mimeType expected mime type of the document
     * @param size expected size of the document
     */
    private fun validateResponseHeaders(uploadResponse: DocumentUploadResponse, mimeType: MediaType, size: String) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until {
                try {
                    val response = documentControllerClient.getDocumentWithHttpInfo(uploadResponse.documentId).headers
                    assertEquals(response[HttpHeaders.CONTENT_LENGTH]?.first(), size)
                    assertEquals(response[HttpHeaders.CONTENT_TYPE]?.first(), mimeType.toString())
                    true
                } catch (e: ClientException) {
                    e.statusCode != HttpStatus.NOT_FOUND.value()
                }
            }
    }

    /**
     * checks if byte array represents a pdf document
     * @param byteArray byte array
     * @returns boolean if byte array represents a pdf
     */
    private fun isByteArrayRepresentationOfPdf(byteArray: ByteArray): Boolean {
        val pdfSignature = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        return byteArray.size >= pdfSignature.size && byteArray.sliceArray(pdfSignature.indices)
            .contentEquals(pdfSignature)
    }
}
