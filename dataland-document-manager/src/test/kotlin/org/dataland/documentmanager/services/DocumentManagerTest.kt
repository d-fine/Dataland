package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.util.Optional

@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
class DocumentManagerTest(
    @Autowired val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired private val fileProcessor: FileProcessor,
) {
    lateinit var mockStorageApi: StreamingStorageControllerApi
    lateinit var mockDocumentMetaInfoRepository: DocumentMetaInfoRepository
    lateinit var mockSecurityContext: SecurityContext
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var documentManager: DocumentManager
    lateinit var mockUserRolesChecker: UserRolesChecker

    val testDocument = "sample.pdf"

    @BeforeEach
    fun mockStorageApi() {
        mockUserRolesChecker = mock(UserRolesChecker::class.java)
        mockSecurityContext = mock(SecurityContext::class.java)
        mockStorageApi = mock(StreamingStorageControllerApi::class.java)
        mockDocumentMetaInfoRepository = mock(DocumentMetaInfoRepository::class.java)
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "data_uploader",
                userId = "dummy-user-id",
                roles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)

        documentManager =
            DocumentManager(
                inMemoryDocumentStore = inMemoryDocumentStore,
                documentMetaInfoRepository = mockDocumentMetaInfoRepository,
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                storageApi = mockStorageApi,
                fileProcessor = fileProcessor,
            )
    }

    @Test
    fun `check that document retrieval is not possible if document does not exist`() {
        assertThrows<ResourceNotFoundApiException> { documentManager.retrieveDocumentById(documentId = "123") }
    }

    @Test
    fun `check that document upload works and that document retrieval is not possible on non QAed documents`() {
        val mockMultipartFile = mockUploadableFile(testDocument)
        val sampleDocMetaInfo = sampleDocumentMetaInfo()

        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, sampleDocMetaInfo)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentType = DocumentType.Pdf,
                        documentName = "sample.pdf",
                        documentCategory = DocumentCategory.AnnualReport,
                        companyIds = mutableListOf<String>(),
                        publicationDate = LocalDate.parse("2023-01-01"),
                        reportingPeriods = mutableListOf("2023"),
                        documentId = uploadResponse.documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Pending,
                    ),
                ),
            )
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                documentManager.retrieveDocumentById(
                    documentId = uploadResponse.documentId,
                )
            }
        assertEquals(
            "A non-quality-assured document with ID: ${uploadResponse.documentId} was found. " +
                "Only quality-assured documents can be retrieved.",
            thrown.message.replaceAfterLast(".", ""),
        )
    }

    @Test
    fun `check that document retrieval is possible on QAed documents`() {
        val mockMultipartFile = mockUploadableFile(testDocument)
        val uploadResponse = mockUpload(mockMultipartFile)
        configureMockDocumentMetaInfoRepository(uploadResponse.documentId)
        val downloadedDocument = documentManager.retrieveDocumentById(documentId = uploadResponse.documentId)
        assertTrue(downloadedDocument.content.contentAsByteArray.contentEquals(mockMultipartFile.bytes))
    }

    @Test
    fun `check that exception is thrown when sending notification to message queue fails during document storage`() {
        val mockMultipartFile = mockUploadableFile(testDocument)
        val sampleDocMetaInfo = sampleDocumentMetaInfo()
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(), eq(MessageType.DOCUMENT_RECEIVED), anyString(),
                eq(ExchangeName.DOCUMENT_RECEIVED), eq(""),
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, sampleDocMetaInfo)
        }
    }

    @Test
    fun `check that a patch request for a non-existing documentId throws the appropriate exception`() {
        val unknownDocumentId = "unknown-document-id"
        val patchObject =
            DocumentMetaInfoPatch(
                documentName = null,
                documentCategory = null,
                companyIds = null,
                publicationDate = null,
                reportingPeriods = null,
            )
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformation(unknownDocumentId, patchObject)
        }
    }

    @Test
    fun `check that a patch request for an existing documentId results in the desired changes`() {
        val mockMultipartFile = mockUploadableFile(testDocument)
        val uploadResponse = mockUpload(mockMultipartFile)
        configureMockDocumentMetaInfoRepository(uploadResponse.documentId)
        val patchObject =
            DocumentMetaInfoPatch(
                documentName = "new name",
                documentCategory = DocumentCategory.SustainabilityReport,
                companyIds = null,
                publicationDate = "2023-01-03",
                reportingPeriods = null,
            )
        val firstDocumentMetaInfoEntity = sampleDocumentMetaInfoEntity(uploadResponse.documentId)
        val expectedSecondDocumentMetaInfoEntity =
            firstDocumentMetaInfoEntity.apply {
                documentName = "new name"
                documentCategory = DocumentCategory.SustainabilityReport
                publicationDate = "2023-01-03"
            }
        `when`(mockDocumentMetaInfoRepository.existsById(anyString())).thenReturn(true)
        // Rewrite the stubbed save method so it acts as an identity function rather than a constant function.
        // It is unclear to me how I can refer to the value of the any() parameter inside thenReturn().
        `when`(mockDocumentMetaInfoRepository.save<DocumentMetaInfoEntity>(any())).thenReturn(
            expectedSecondDocumentMetaInfoEntity,
        )
        `when`(mockDocumentMetaInfoRepository.getByDocumentId(anyString())).thenReturn(
            firstDocumentMetaInfoEntity,
        )
        val patchResponse = documentManager.patchDocumentMetaInformation(uploadResponse.documentId, patchObject)

        assertEquals(expectedSecondDocumentMetaInfoEntity.documentName, patchResponse.documentName)
        assertEquals(expectedSecondDocumentMetaInfoEntity.documentCategory, patchResponse.documentCategory)
        assertEquals(expectedSecondDocumentMetaInfoEntity.publicationDate, patchResponse.publicationDate)
    }

    private fun mockUploadableFile(reportName: String): MockMultipartFile {
        val testFileStream = javaClass.getResourceAsStream("sampleFiles/$reportName")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        return MockMultipartFile(
            reportName, reportName,
            "application/pdf", testFileBytes,
        )
    }

    private fun mockUpload(mockMultipartFile: MockMultipartFile): DocumentUploadResponse {
        val sampleDocMetaInfo = sampleDocumentMetaInfo()
        return documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, sampleDocMetaInfo)
    }

    private fun sampleDocumentMetaInfoEntity(documentId: String): DocumentMetaInfoEntity =
        DocumentMetaInfoEntity(
            documentType = DocumentType.Pdf,
            documentName = "sample.pdf",
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = listOf(),
            publicationDate = "2023-01-01",
            reportingPeriods = listOf("2023"),
            documentId = documentId,
            uploaderId = "",
            uploadTime = 0,
            qaStatus = QaStatus.Accepted,
        )

    private fun configureMockDocumentMetaInfoRepository(documentId: String) {
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    sampleDocumentMetaInfoEntity(documentId),
                ),
            )
    }

    private fun sampleDocumentMetaInfo(): DocumentMetaInfo =
        DocumentMetaInfo(
            documentName = "sample.pdf",
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = listOf("someValidId"),
            publicationDate = "2023-01-01",
            reportingPeriods = listOf("2023"),
        )
}
