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
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        val mockDocMetaInfo = mockDocumentMetaInfo()

        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, mockDocMetaInfo)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentType = DocumentType.Pdf,
                        documentName = "sample.pdf",
                        documentCategory = DocumentCategory.AnnualReport,
                        companyIds = listOf(),
                        publicationDate = "2023-01-01",
                        reportingPeriod = "2023",
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
        val mockDocMetaInfo = mockDocumentMetaInfo()
        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, mockDocMetaInfo)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentType = DocumentType.Pdf,
                        documentName = "sample.pdf",
                        documentCategory = DocumentCategory.AnnualReport,
                        companyIds = listOf(),
                        publicationDate = "2023-01-01",
                        reportingPeriod = "2023",
                        documentId = uploadResponse.documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Accepted,
                    ),
                ),
            )
        val downloadedDocument = documentManager.retrieveDocumentById(documentId = uploadResponse.documentId)
        assertTrue(downloadedDocument.content.contentAsByteArray.contentEquals(mockMultipartFile.bytes))
    }

    @Test
    fun `check that exception is thrown when sending notification to message queue fails during document storage`() {
        val mockMultipartFile = mockUploadableFile(testDocument)
        val mockDocMetaInfo = mockDocumentMetaInfo()
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(), eq(MessageType.DOCUMENT_RECEIVED), anyString(),
                eq(ExchangeName.DOCUMENT_RECEIVED), eq(""),
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile, mockDocMetaInfo)
        }
    }

    private fun mockUploadableFile(reportName: String): MockMultipartFile {
        val testFileStream = javaClass.getResourceAsStream("sampleFiles/$reportName")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        return MockMultipartFile(
            reportName, reportName,
            "application/pdf", testFileBytes,
        )
    }

    private fun mockDocumentMetaInfo(): DocumentMetaInfo {
        val mockDocInfo = mock(DocumentMetaInfo::class.java)
        `when`(mockDocInfo.documentName).thenReturn("sample.pdf")
        `when`(mockDocInfo.documentCategory).thenReturn(DocumentCategory.AnnualReport)
        `when`(mockDocInfo.companyIds).thenReturn(listOf())
        `when`(mockDocInfo.publicationDate).thenReturn("2023-01-01")
        `when`(mockDocInfo.reportingPeriod).thenReturn("2023")
        return mockDocInfo
    }
}
