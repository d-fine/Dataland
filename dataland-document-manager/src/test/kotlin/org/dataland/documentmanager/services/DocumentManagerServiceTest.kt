package org.dataland.documentmanager.services

import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.model.DocumentMetaInformationSearchFilter
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import java.time.LocalDate

@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@Suppress("LongParameterList")
class DocumentManagerServiceTest(
    @Autowired val documentMetaInfoRepository: DocumentMetaInfoRepository,
) {
    val mockInMemoryDocumentStore = mock<InMemoryDocumentStore>()
    val mockStorageApi = mock<StreamingStorageControllerApi>()
    val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    val mockFileProcessor = mock<FileProcessor>()
    val mockSecurityContext = mock<SecurityContext>()
    val mockAuthentication = mock<DatalandJwtAuthentication>()

    lateinit var documentManager: DocumentManager

    private val companyId1 = "company-id-1"
    private val companyId2 = "company-id-2"
    private val companyId3 = "company-id-3"

    private val documentName1 = "document-1"
    private val documentName2 = "document-2"
    private val documentName3 = "document-3"

    private val documentId1 = "document-id-1"
    private val documentId2 = "document-id-2"
    private val documentId3 = "document-id-3"

    private val uploaderId1 = "uploader-1"
    private val uploaderId2 = "uploader-2"

    private val date1 = "2024-01-01"
    private val date2 = "2024-06-30"
    private val date3 = "2023-06-30"

    private val reportingPeriod2023 = "2023"
    private val reportingPeriod2024 = "2024"

    private val documentMetaInfoEntity1 =
        DocumentMetaInfoEntity(
            documentId = documentId1,
            documentType = DocumentType.Pdf,
            documentName = documentName1,
            documentCategory = DocumentCategory.SustainabilityReport,
            companyIds = mutableSetOf(companyId1),
            uploaderId = uploaderId1,
            uploadTime = 0,
            publicationDate = LocalDate.parse(date1),
            reportingPeriod = reportingPeriod2023,
            qaStatus = QaStatus.Accepted,
        )

    private val documentMetaInfoEntity2 =
        DocumentMetaInfoEntity(
            documentId = documentId2,
            documentType = DocumentType.Pdf,
            documentName = documentName2,
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(companyId1, companyId2),
            uploaderId = uploaderId1,
            uploadTime = 0,
            publicationDate = LocalDate.parse(date2),
            reportingPeriod = reportingPeriod2024,
            qaStatus = QaStatus.Accepted,
        )

    private val documentMetaInfoEntity3 =
        DocumentMetaInfoEntity(
            documentId = documentId3,
            documentType = DocumentType.Pdf,
            documentName = documentName3,
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(companyId3),
            uploaderId = uploaderId2,
            uploadTime = 0,
            publicationDate = LocalDate.parse(date3),
            reportingPeriod = reportingPeriod2023,
            qaStatus = QaStatus.Accepted,
        )

    init {
        documentMetaInfoRepository.save(documentMetaInfoEntity1)
        documentMetaInfoRepository.save(documentMetaInfoEntity2)
        documentMetaInfoRepository.save(documentMetaInfoEntity3)
    }

    @BeforeEach
    fun setup() {
        reset(
            mockInMemoryDocumentStore,
            mockStorageApi,
            mockCloudEventMessageHandler,
            mockFileProcessor,
            mockSecurityContext,
            mockAuthentication,
        )
        val authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                "requester@example.com",
                "1234-221-1111elf",
                setOf(DatalandRealmRole.ROLE_USER),
            )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)

        documentManager =
            DocumentManager(
                documentMetaInfoRepository = documentMetaInfoRepository,
                inMemoryDocumentStore = mockInMemoryDocumentStore,
                storageApi = mockStorageApi,
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                fileProcessor = mockFileProcessor,
            )
    }

    @Test
    fun `check that a search by company id yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                companyId = companyId1,
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity1.toDocumentMetaInfoResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity2.toDocumentMetaInfoResponse()))
    }

    @Test
    fun `check that a search by a single document category yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                documentCategories = setOf(DocumentCategory.AnnualReport),
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity2.toDocumentMetaInfoResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity3.toDocumentMetaInfoResponse()))
    }

    @Test
    fun `check that a search by multiple document categories yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                companyId = companyId1,
                documentCategories =
                    setOf(
                        DocumentCategory.SustainabilityReport,
                        DocumentCategory.AnnualReport,
                    ),
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity1.toDocumentMetaInfoResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity2.toDocumentMetaInfoResponse()))
    }

    @Test
    fun `check that a search by reporting period yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                reportingPeriod = reportingPeriod2023,
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity1.toDocumentMetaInfoResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity3.toDocumentMetaInfoResponse()))
    }
}
