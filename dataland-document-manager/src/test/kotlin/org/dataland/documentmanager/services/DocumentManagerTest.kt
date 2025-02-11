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
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
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
    private val mockStorageApi = mock<StreamingStorageControllerApi>()
    private val mockDocumentMetaInfoRepository = mock<DocumentMetaInfoRepository>()
    private val mockSecurityContext = mock<SecurityContext>()
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockUserRolesChecker = mock<UserRolesChecker>()
    lateinit var documentManager: DocumentManager

    private val mockDocumentName = "sample.pdf"
    private val unknownDocumentId = "unknown-document-id"
    private val knownCompanyIdOne = "knownCompanyId1"

    private val dummyDocumentMetaInfo =
        DocumentMetaInfo(
            documentName = mockDocumentName,
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableListOf(knownCompanyIdOne),
            publicationDate = LocalDate.parse("2023-01-01"),
            reportingPeriod = "2023",
        )

    @BeforeEach
    fun setup() {
        reset(
            mockStorageApi,
            mockDocumentMetaInfoRepository,
            mockSecurityContext,
            mockCloudEventMessageHandler,
            mockUserRolesChecker,
        )

        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "data_uploader",
                userId = "dummy-user-id",
                roles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
            )

        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
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

    private fun setupMockDocument(): MockMultipartFile {
        val testFileStream = javaClass.getResourceAsStream("sampleFiles/$mockDocumentName")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        return MockMultipartFile(
            mockDocumentName, mockDocumentName,
            "application/pdf", testFileBytes,
        )
    }

    private fun buildDocumentMetaInfoEntity(
        documentId: String,
        qaStatus: QaStatus = QaStatus.Pending,
    ): DocumentMetaInfoEntity =
        DocumentMetaInfoEntity(
            documentId = documentId,
            documentType = DocumentType.Pdf,
            documentName = dummyDocumentMetaInfo.documentName,
            documentCategory = dummyDocumentMetaInfo.documentCategory,
            companyIds = dummyDocumentMetaInfo.companyIds.toMutableList(),
            publicationDate = dummyDocumentMetaInfo.publicationDate,
            reportingPeriod = dummyDocumentMetaInfo.reportingPeriod,
            uploaderId = "",
            uploadTime = 0,
            qaStatus = qaStatus,
        )

    @Test
    fun `check that document retrieval is not possible if document does not exist`() {
        assertThrows<ResourceNotFoundApiException> { documentManager.retrieveDocumentById(documentId = "123") }
    }

    @Test
    fun `check that document upload works and that document retrieval is not possible on non QAed documents`() {
        val mockDocument = setupMockDocument()
        val uploadResponse =
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockDocument, dummyDocumentMetaInfo)
        val pendingDocumentMetaInfoEntity = buildDocumentMetaInfoEntity(uploadResponse.documentId)
        doReturn(Optional.of(pendingDocumentMetaInfoEntity))
            .whenever(mockDocumentMetaInfoRepository)
            .findById(uploadResponse.documentId)

        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                documentManager.retrieveDocumentById(documentId = uploadResponse.documentId)
            }
        assertEquals(
            "A non-quality-assured document with ID: ${uploadResponse.documentId} was found. " +
                "Only quality-assured documents can be retrieved.",
            thrown.message.replaceAfterLast(".", ""),
        )
    }

    @Test
    fun `check that document retrieval is possible on QAed documents`() {
        val mockDocument = setupMockDocument()
        val uploadResponse =
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockDocument, dummyDocumentMetaInfo)
        val acceptedDocumentMetaInfoEntity = buildDocumentMetaInfoEntity(uploadResponse.documentId, QaStatus.Accepted)
        doReturn(Optional.of(acceptedDocumentMetaInfoEntity))
            .whenever(mockDocumentMetaInfoRepository)
            .findById(uploadResponse.documentId)
        val downloadedDocument = documentManager.retrieveDocumentById(documentId = uploadResponse.documentId)
        assertTrue(downloadedDocument.content.contentAsByteArray.contentEquals(mockDocument.bytes))
    }

    @Test
    fun `check that exception is thrown when sending notification to message queue fails during document storage`() {
        val mockDocument = setupMockDocument()

        doThrow(AmqpException::class).whenever(mockCloudEventMessageHandler).buildCEMessageAndSendToQueue(
            any(), eq(MessageType.DOCUMENT_RECEIVED), any(),
            eq(ExchangeName.DOCUMENT_RECEIVED), eq(""),
        )

        assertThrows<AmqpException> {
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockDocument, dummyDocumentMetaInfo)
        }
    }

    @Test
    fun `check that a patching metadata for a non existing documentId throws the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformation(unknownDocumentId, mock<DocumentMetaInfoPatch>())
        }
    }

    @Test
    fun `check that adding a companyId to an unknown document throws exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformationCompanyIds(unknownDocumentId, "some-company-id")
        }
    }

    @Test
    fun `check that patching meta data for an existing documentId results in the desired changes`() {
        val mockDocument = setupMockDocument()
        val uploadResponse =
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockDocument, dummyDocumentMetaInfo)

        val dummyDocumentMetaInfoEntity = buildDocumentMetaInfoEntity(uploadResponse.documentId)
        val documentMetaInfoPatch =
            DocumentMetaInfoPatch(
                documentName = "new name",
                documentCategory = DocumentCategory.SustainabilityReport,
                companyIds = listOf("company-id-2", "company-id-3"),
                publicationDate = LocalDate.parse("2023-01-03"),
                reportingPeriod = null,
            )

        doReturn(true).whenever(mockDocumentMetaInfoRepository).existsById(any())
        doReturn(dummyDocumentMetaInfoEntity)
            .whenever(mockDocumentMetaInfoRepository)
            .getByDocumentId(uploadResponse.documentId)
        doReturn(dummyDocumentMetaInfoEntity).whenever(mockDocumentMetaInfoRepository).save(dummyDocumentMetaInfoEntity)

        assertDoesNotThrow {
            documentManager.patchDocumentMetaInformation(
                uploadResponse.documentId,
                documentMetaInfoPatch,
            )
        }

        assertEquals(documentMetaInfoPatch.documentName, dummyDocumentMetaInfoEntity.documentName)
        assertEquals(documentMetaInfoPatch.documentCategory, dummyDocumentMetaInfoEntity.documentCategory)
        assertEquals(documentMetaInfoPatch.companyIds, dummyDocumentMetaInfoEntity.companyIds)
        assertEquals(documentMetaInfoPatch.publicationDate, dummyDocumentMetaInfoEntity.publicationDate)
        assertEquals(dummyDocumentMetaInfo.reportingPeriod, dummyDocumentMetaInfoEntity.reportingPeriod)
    }

    @Test
    fun `check that patching companyIds for an existing documentId results in the desired change`() {
        val mockDocument = setupMockDocument()
        val uploadResponse =
            documentManager.temporarilyStoreDocumentAndTriggerStorage(mockDocument, dummyDocumentMetaInfo)
        val dummyDocumentMetaInfoEntity = buildDocumentMetaInfoEntity(uploadResponse.documentId)
        val newCompanyId = "newlyAddedCompanyId"

        doReturn(true).whenever(mockDocumentMetaInfoRepository).existsById(any())
        doReturn(dummyDocumentMetaInfoEntity)
            .whenever(mockDocumentMetaInfoRepository)
            .getByDocumentId(uploadResponse.documentId)
        doReturn(dummyDocumentMetaInfoEntity).whenever(mockDocumentMetaInfoRepository).save(dummyDocumentMetaInfoEntity)

        assertDoesNotThrow {
            documentManager.patchDocumentMetaInformationCompanyIds(
                uploadResponse.documentId,
                newCompanyId,
            )
        }

        val expectedListOfCompanyIds = dummyDocumentMetaInfo.companyIds.plus(newCompanyId)

        assertEquals(dummyDocumentMetaInfo.documentName, dummyDocumentMetaInfoEntity.documentName)
        assertEquals(dummyDocumentMetaInfo.documentCategory, dummyDocumentMetaInfoEntity.documentCategory)
        assertEquals(expectedListOfCompanyIds, dummyDocumentMetaInfoEntity.companyIds)
        assertEquals(dummyDocumentMetaInfo.publicationDate, dummyDocumentMetaInfoEntity.publicationDate)
        assertEquals(dummyDocumentMetaInfo.reportingPeriod, dummyDocumentMetaInfoEntity.reportingPeriod)
    }
}
