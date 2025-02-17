package org.dataland.documentmanager.services

import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.dataland.documentmanager.services.conversion.FileProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
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

    lateinit var documentManager: DocumentManager

    private val companyId1 = "company-id-1"
    private val companyId2 = "company-id-2"
    private val companyId3 = "company-id-3"

    private val documentId1 = "document-id-1"
    private val documentId2 = "document-id-2"
    private val documentId3 = "document-id-3"

    private val documentMetaInfoEntity1 =
        DocumentMetaInfoEntity(
            documentId = documentId1,
            documentType = DocumentType.Pdf,
            documentName = "document-1",
            documentCategory = DocumentCategory.SustainabilityReport,
            companyIds = mutableSetOf(companyId1),
            uploaderId = "uploader-1",
            uploadTime = 0,
            publicationDate = LocalDate.parse("2024-01-01"),
            reportingPeriod = "2023",
            qaStatus = QaStatus.Accepted,
        )

    private val documentMetaInfoEntity2 =
        DocumentMetaInfoEntity(
            documentId = documentId2,
            documentType = DocumentType.Pdf,
            documentName = "document-2",
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(companyId1, companyId2),
            uploaderId = "uploader-1",
            uploadTime = 0,
            publicationDate = LocalDate.parse("2024-06-30"),
            reportingPeriod = "2024",
            qaStatus = QaStatus.Accepted,
        )

    private val documentMetaInfoEntity3 =
        DocumentMetaInfoEntity(
            documentId = documentId3,
            documentType = DocumentType.Pdf,
            documentName = "document-3",
            documentCategory = DocumentCategory.AnnualReport,
            companyIds = mutableSetOf(companyId3),
            uploaderId = "uploader-2",
            uploadTime = 0,
            publicationDate = LocalDate.parse("2023-06-30"),
            reportingPeriod = "2023",
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
        )

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
        assertTrue(searchResults.contains(documentMetaInfoEntity1.toDocumentUploadResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity2.toDocumentUploadResponse()))
    }

    @Test
    fun `check that a search by document category yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                documentCategory = DocumentCategory.AnnualReport,
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity2.toDocumentUploadResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity3.toDocumentUploadResponse()))
    }

    @Test
    fun `check that a search by reporting period yields the expected results`() {
        val documentMetaInformationSearchFilter =
            DocumentMetaInformationSearchFilter(
                reportingPeriod = "2023",
            )

        val searchResults =
            documentManager.searchForDocumentMetaInformation(
                documentMetaInformationSearchFilter,
            )

        assertEquals(searchResults.size, 2)
        assertTrue(searchResults.contains(documentMetaInfoEntity1.toDocumentUploadResponse()))
        assertTrue(searchResults.contains(documentMetaInfoEntity3.toDocumentUploadResponse()))
    }
}
