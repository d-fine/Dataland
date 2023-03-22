package org.dataland.documentmanager.services

import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.io.File
import java.util.*

@SpringBootTest(classes = [DatalandDocumentManager::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DocumentManagerTest(
    @Autowired val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired private val pdfVerificationService: PdfVerificationService,
) {
    lateinit var mockStorageApi: StorageControllerApi
    lateinit var mockDocumentMetaInfoRepository: DocumentMetaInfoRepository
    lateinit var mockSecurityContext: SecurityContext
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var documentManager: DocumentManager

    @BeforeEach
    fun mockStorageApi() {
        mockSecurityContext = mock(SecurityContext::class.java)
        mockStorageApi = mock(StorageControllerApi::class.java)
        mockDocumentMetaInfoRepository = mock(DocumentMetaInfoRepository::class.java)
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
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
            messageUtils = messageUtils,
            pdfVerificationService = pdfVerificationService,
            storageApi = mockStorageApi,
        )
    }

    @Test
    fun `check that document retrieval is not possible on non QAed documents`() {
        val file = File("./public/test-report.pdf")
        val mockMultipartFile = MockMultipartFile(
            "test-report.pdf", "test-report.pdf",
            "application/pdf", file.readBytes(),
        )
        val metaInfo = documentManager.temporarilyStoreDocumentAndTriggerStorage(mockMultipartFile)
        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(Optional.of(DocumentMetaInfoEntity(metaInfo)))
        assertThrows<ResourceNotFoundApiException> { documentManager.retrieveDocumentById(documentId = metaInfo.documentId) }
    }
}
