package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
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
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.dataland.documentmanager.model.DocumentMetaInformationSearchFilter
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

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
    private val unknownDocumentId = "unknownDocumentId"
    private val knownCompanyId = "knownCompanyId"
    private val knownDocumentId = "known-document-id"
    private val knownButNonRetrievableDocumentId = "knownButNonRetrievableDocumentId"

    private val dummyDocumentMetaInfo =
        DocumentMetaInfo(
            documentName = mockDocumentName,
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(knownCompanyId),
            publicationDate = LocalDate.parse("2023-01-01"),
            reportingPeriod = "2023",
        )

    private val dummyDocumentUploadResponse =
        DocumentMetaInfoResponse(
            documentId = knownDocumentId,
            documentName = mockDocumentName,
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(knownCompanyId),
            uploaderId = "dummy-uploader-id",
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

        doReturn(true).whenever(mockDocumentMetaInfoRepository).existsById(knownButNonRetrievableDocumentId)
        doReturn(null).whenever(mockDocumentMetaInfoRepository).getByDocumentId(knownButNonRetrievableDocumentId)
    }

    private fun setupMockDocument(): MockMultipartFile {
        val testFileStream = javaClass.getResourceAsStream("sampleFiles/$mockDocumentName")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        return MockMultipartFile(
            mockDocumentName, mockDocumentName,
            "application/pdf", testFileBytes,
        )
    }

    private fun storeDocumentAndMetaInfo(
        document: MultipartFile,
        metaInfo: DocumentMetaInfo,
    ): DocumentMetaInfoResponse =
        documentManager
            .temporarilyStoreDocumentAndTriggerStorage(document, metaInfo)

    private fun buildDocumentMetaInfoEntityWithDocumentId(
        documentId: String,
        qaStatus: QaStatus = QaStatus.Pending,
    ): DocumentMetaInfoEntity =
        DocumentMetaInfoEntity(
            documentId = documentId,
            documentType = DocumentType.Pdf,
            documentName = dummyDocumentMetaInfo.documentName,
            documentCategory = dummyDocumentMetaInfo.documentCategory,
            companyIds = dummyDocumentMetaInfo.companyIds.toMutableSet(),
            publicationDate = dummyDocumentMetaInfo.publicationDate,
            reportingPeriod = dummyDocumentMetaInfo.reportingPeriod,
            uploaderId = "dummy-uploader-id",
            uploadTime = 0,
            qaStatus = qaStatus,
        )

    @Test
    fun `check that document retrieval is not possible if document does not exist`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocument(unknownDocumentId)
        }
    }

    @Test
    fun `check that retrieving an existing but nonretrievable document throws the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocument(knownButNonRetrievableDocumentId)
        }
    }

    @Test
    fun `check that document meta info retrieval is not possible if document does not exist`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocumentMetaInfo(unknownDocumentId)
        }
    }

    @Test
    fun `check that retrieving meta info of an existing but nonretrievable document throws the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocumentMetaInfo(knownButNonRetrievableDocumentId)
        }
    }

    @Test
    fun `check that trying to retrieve the metainfo of an existing but nonretrievable document throws exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.retrieveDocumentMetaInfo(knownButNonRetrievableDocumentId)
        }
    }

    @Test
    fun `check that document upload works and that document retrieval is not possible on non QAed documents`() {
        val mockDocument = setupMockDocument()
        val uploadResponse = storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo)
        val pendingDocumentMetaInfoEntity = buildDocumentMetaInfoEntityWithDocumentId(uploadResponse.documentId)

        doReturn(pendingDocumentMetaInfoEntity)
            .whenever(mockDocumentMetaInfoRepository)
            .getByDocumentId(uploadResponse.documentId)

        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                documentManager.retrieveDocument(documentId = uploadResponse.documentId)
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
        val uploadResponse = storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo)
        val acceptedDocumentMetaInfoEntity =
            buildDocumentMetaInfoEntityWithDocumentId(uploadResponse.documentId, QaStatus.Accepted)

        doReturn(acceptedDocumentMetaInfoEntity)
            .whenever(mockDocumentMetaInfoRepository)
            .getByDocumentId(uploadResponse.documentId)
        val downloadedDocument = documentManager.retrieveDocument(documentId = uploadResponse.documentId)
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
    fun `check that uploading a document twice throws a conflict exception`() {
        val mockDocument = setupMockDocument()
        val uploadResponse = storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo)

        doReturn(true).whenever(mockDocumentMetaInfoRepository).existsById(uploadResponse.documentId)
        assertThrows<ConflictApiException> { storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo) }
    }

    @Test
    fun `check that patching metadata for a non existing documentId throws the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformation(unknownDocumentId, mock<DocumentMetaInfoPatch>())
        }
    }

    @Test
    fun `check that patching metadata for an existing but nonretrievable document throws the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformation(
                knownButNonRetrievableDocumentId,
                mock<DocumentMetaInfoPatch>(),
            )
        }
    }

    @Test
    fun `check that adding a companyId to an unknown document throws exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformationCompanyIds(
                unknownDocumentId,
                knownCompanyId,
            )
        }
    }

    @Test
    fun `check that adding a companyId to an existing but nonretrievable document throws exception`() {
        assertThrows<ResourceNotFoundApiException> {
            documentManager.patchDocumentMetaInformationCompanyIds(
                knownButNonRetrievableDocumentId,
                knownCompanyId,
            )
        }
    }

    @Test
    fun `check that patching meta data for an existing documentId results in the desired changes`() {
        val mockDocument = setupMockDocument()
        val uploadResponse = storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo)
        val dummyDocumentMetaInfoEntity = buildDocumentMetaInfoEntityWithDocumentId(uploadResponse.documentId)

        val documentMetaInfoPatch =
            DocumentMetaInfoPatch(
                documentName = "new name",
                documentCategory = DocumentCategory.SustainabilityReport,
                companyIds = setOf("company-id-2", "company-id-3"),
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
        val uploadResponse = storeDocumentAndMetaInfo(mockDocument, dummyDocumentMetaInfo)
        val dummyDocumentMetaInfoEntity = buildDocumentMetaInfoEntityWithDocumentId(uploadResponse.documentId)

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

    private fun setupDocumentIdToBeFound(documentId: String) {
        doReturn(true).whenever(mockDocumentMetaInfoRepository).existsById(documentId)
        doReturn(buildDocumentMetaInfoEntityWithDocumentId(documentId))
            .whenever(
                mockDocumentMetaInfoRepository,
            ).getByDocumentId(documentId)
    }

    @Test
    fun `check that retrieval of document metainfo works for an existing document id`() {
        val response1 = dummyDocumentUploadResponse
        setupDocumentIdToBeFound(response1.documentId)
        val response2 = documentManager.retrieveDocumentMetaInfo(response1.documentId).toDocumentMetaInfoResponse()
        assertEquals(response1, response2)
    }

    @Test
    fun `check that the search filter is built correctly in a search request with no specified chunkSize`() {
        doReturn(listOf<DocumentMetaInfoEntity>())
            .whenever(mockDocumentMetaInfoRepository)
            .findByCompanyIdAndDocumentCategoryAndReportingPeriod(
                companyId = any(),
                documentCategories = any(),
                reportingPeriod = any(),
                limit = any(),
                offset = any(),
            )
        val searchFilter =
            DocumentMetaInformationSearchFilter(
                companyId = knownCompanyId,
                documentCategories = null,
                reportingPeriod = "2023",
            )
        documentManager.searchForDocumentMetaInformation(searchFilter)
        verify(mockDocumentMetaInfoRepository)
            .findByCompanyIdAndDocumentCategoryAndReportingPeriod(
                companyId = searchFilter.companyId,
                documentCategories = null,
                reportingPeriod = "2023",
            )
    }

    @Test
    fun `check that search filter and limit and offset are built correctly in a search request`() {
        doReturn(listOf<DocumentMetaInfoEntity>())
            .whenever(mockDocumentMetaInfoRepository)
            .findByCompanyIdAndDocumentCategoryAndReportingPeriod(
                companyId = any(),
                documentCategories = any(),
                reportingPeriod = any(),
                limit = any(),
                offset = any(),
            )
        val searchFilter =
            DocumentMetaInformationSearchFilter(
                companyId = knownCompanyId,
                documentCategories = null,
                reportingPeriod = "2023",
            )
        documentManager.searchForDocumentMetaInformation(searchFilter, chunkSize = 50, chunkIndex = 3)
        verify(mockDocumentMetaInfoRepository)
            .findByCompanyIdAndDocumentCategoryAndReportingPeriod(
                companyId = knownCompanyId,
                documentCategories = null,
                reportingPeriod = "2023",
                limit = 50,
                offset = 150,
            )
    }
}
