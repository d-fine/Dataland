package org.dataland.documentmanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentQAStatus
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
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
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@SpringBootTest(classes = [DatalandDocumentManager::class])
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

        val metaInfo = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(Optional.of(DocumentMetaInfoEntity(metaInfo)))
        val thrown = assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocumentById(
                documentId = metaInfo.documentId,
            )
        }
        assertEquals(
            "A non-quality-assured document with ID: ${metaInfo.documentId} was found. " +
                "Only quality-assured documents can be retrieved.",
            thrown.message.replaceAfterLast(".", ""),
        )
    }

    @Test
    fun `check that document retrieval is possible on QAed documents`() {
        val mockMultipartFile = mockUploadableFile(reportName)
        val metaInfo = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        metaInfo.qaStatus = DocumentQAStatus.Accepted
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(Optional.of(DocumentMetaInfoEntity(metaInfo)))
        val downloadedDocument = documentManager.retrieveDocumentById(documentId = metaInfo.documentId)
        assertEquals(reportName, downloadedDocument.title)
        assertTrue(downloadedDocument.content.contentAsByteArray.contentEquals(mockMultipartFile.bytes))
    }

    @Test
    fun `check that an exception is thrown in updating of meta data when documentId is empty`() {
        val messageWithEmptyDocumentID = objectMapper.writeValueAsString(
            QaCompletedMessage(
                identifier = "",
                validationResult = "By default, QA is passed",
            ),
        )
        val thrown = assertThrows<MessageQueueRejectException> {
            documentManager.updateDocumentMetaData(messageWithEmptyDocumentID, "", MessageType.QACompleted)
        }
        assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check that updating meta data after QA works for an existing document`() {
        val mockMultipartFile = mockUploadableFile(reportName)
        val metaInfo = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(
                identifier = metaInfo.documentId,
                validationResult = "By default, QA is passed",
            ),
        )

        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(Optional.of(DocumentMetaInfoEntity(metaInfo)))

        assertDoesNotThrow { documentManager.updateDocumentMetaData(message, "", MessageType.QACompleted) }
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
                eq(ExchangeNames.documentReceived), eq(""),
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
