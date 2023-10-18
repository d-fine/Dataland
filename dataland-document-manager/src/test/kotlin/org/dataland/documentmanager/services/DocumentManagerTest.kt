package org.dataland.documentmanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DocumentManagerTest(
    @Autowired val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired private val pdfVerificationService: PdfVerificationService,
    @Autowired private var objectMapper: ObjectMapper,
) {

    lateinit var mockStorageApi: StreamingStorageControllerApi
    lateinit var mockDocumentMetaInfoRepository: DocumentMetaInfoRepository
    lateinit var mockSecurityContext: SecurityContext
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var documentManager: DocumentManager
    lateinit var mockMessageUtils: MessageQueueUtils
    val reportName = "test-report.pdf"

    @BeforeEach
    fun mockStorageApi() {
        mockSecurityContext = mock(SecurityContext::class.java)
        mockStorageApi = mock(StreamingStorageControllerApi::class.java)
        mockDocumentMetaInfoRepository = mock(DocumentMetaInfoRepository::class.java)
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        mockMessageUtils = mock(MessageQueueUtils::class.java)
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
            username = "data_uploader",
            userId = "dummy-user-id",
            roles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)

        documentManager = DocumentManager(
            inMemoryDocumentStore = inMemoryDocumentStore,
            documentMetaInfoRepository = mockDocumentMetaInfoRepository,
            cloudEventMessageHandler = mockCloudEventMessageHandler,
            pdfVerificationService = pdfVerificationService,
            storageApi = mockStorageApi,
            objectMapper = objectMapper,
        )
    }

    @Test
    fun `check that document retrieval is not possible if document does not exist`() {
        assertThrows<ResourceNotFoundApiException> { documentManager.retrieveDocumentById(documentId = "123") }
    }

    @Test
    fun `check that document upload works and that document retrieval is not possible on non QAed documents`() {
        val mockMultipartFile = mockUploadableFile(reportName)

        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentId = uploadResponse.documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Pending,
                    ),
                ),
            )
        val thrown = assertThrows<ResourceNotFoundApiException> {
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
        val mockMultipartFile = mockUploadableFile(reportName)
        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentId = uploadResponse.documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Accepted,
                    ),
                ),
            )
        val downloadedDocument = documentManager.retrieveDocumentById(documentId = uploadResponse.documentId)
        assertNotNull(downloadedDocument.title)
        assertTrue(downloadedDocument.content.contentAsByteArray.contentEquals(mockMultipartFile.bytes))
    }

    @Test
    fun `check that an exception is thrown in updating of meta data when documentId is empty`() {
        val messageWithEmptyDocumentID = objectMapper.writeValueAsString(
            QaCompletedMessage(
                identifier = "",
                validationResult = QaStatus.Accepted,
            ),
        )
        val thrown = assertThrows<MessageQueueRejectException> {
            documentManager.updateDocumentMetaData(messageWithEmptyDocumentID, "", MessageType.QaCompleted)
        }
        assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check that updating meta data after QA works for an existing document`() {
        val mockMultipartFile = mockUploadableFile(reportName)

        val uploadResponse = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(
                identifier = uploadResponse.documentId,
                validationResult = QaStatus.Accepted,
            ),
        )

        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentId = uploadResponse.documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Pending,
                    ),
                ),
            )

        assertDoesNotThrow { documentManager.updateDocumentMetaData(message, "", MessageType.QaCompleted) }
    }

    @Test
    fun `check that an exception is thrown in removing of stored document if documentId is empty`() {
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            documentManager.removeStoredDocumentFromTemporaryStore(
                "", "",
                MessageType.DocumentStored,
            )
        }
        assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check that exception is thrown when sending notification to message queue fails during document storage`() {
        val mockMultipartFile = mockUploadableFile(reportName)
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(), eq(MessageType.DocumentReceived), anyString(),
                eq(ExchangeName.DocumentReceived), eq(""),
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        }
    }
    private fun mockUploadableFile(reportName: String): MockMultipartFile {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/$reportName")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        return MockMultipartFile(
            reportName, reportName,
            "application/pdf", testFileBytes,
        )
    }
}
