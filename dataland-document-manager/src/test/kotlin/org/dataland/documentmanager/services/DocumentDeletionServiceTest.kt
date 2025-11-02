package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.openApiClient.model.QaReviewResponse
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.documentmanager.exceptions.DocumentNotFoundException
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DocumentDeletionServiceTest {
    private lateinit var mockDocumentMetaInfoRepository: DocumentMetaInfoRepository
    private lateinit var mockStorageControllerApi: StorageControllerApi
    private lateinit var mockQaControllerApi: QaControllerApi
    private lateinit var documentDeletionService: DocumentDeletionService

    private val testDocumentId = UUID.randomUUID().toString()
    private val testDatasetId1 = UUID.randomUUID().toString()
    private val testDataPointId1 = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        mockDocumentMetaInfoRepository = mock<DocumentMetaInfoRepository>()
        mockStorageControllerApi = mock<StorageControllerApi>()
        mockQaControllerApi = mock<QaControllerApi>()
        documentDeletionService =
            DocumentDeletionService(
                mockDocumentMetaInfoRepository,
                mockStorageControllerApi,
                mockQaControllerApi,
            )
    }

    @Test
    fun `check that document with no references is deleted successfully`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(true)
        whenever(mockStorageControllerApi.getDocumentReferences(any(), any())).thenReturn(
            mapOf(
                "dataPointIds" to emptyList(),
                "datasetIds" to emptyList(),
            ),
        )

        documentDeletionService.deleteDocument(testDocumentId)

        verify(mockStorageControllerApi).deleteDocument(any(), any())
        verify(mockDocumentMetaInfoRepository).deleteById(testDocumentId)
    }

    @Test
    fun `check that document with all references rejected is deleted successfully`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(true)
        whenever(mockStorageControllerApi.getDocumentReferences(any(), any())).thenReturn(
            mapOf(
                "dataPointIds" to listOf(testDataPointId1),
                "datasetIds" to listOf(testDatasetId1),
            ),
        )

        val rejectedDatasetQaReview =
            QaReviewResponse(
                dataId = testDatasetId1,
                companyId = "44444444-4444-4444-4444-444444444444",
                companyName = "Great Company Inc.",
                framework = "sfdr",
                reportingPeriod = "2023",
                timestamp = System.currentTimeMillis(),
                qaStatus = QaStatus.Rejected,
            )
        whenever(mockQaControllerApi.getQaReviewResponseByDataId(UUID.fromString(testDatasetId1)))
            .thenReturn(rejectedDatasetQaReview)

        val rejectedDataPointQaReview =
            DataPointQaReviewInformation(
                dataPointId = testDataPointId1,
                companyId = "44444444-4444-4444-4444-444444444444",
                companyName = "Great Company Inc.",
                dataPointType = "emissions",
                reportingPeriod = "2023",
                timestamp = System.currentTimeMillis(),
                qaStatus = QaStatus.Rejected,
                comment = null,
                reviewerId = "55555555-5555-5555-5555-555555555555",
            )
        whenever(mockQaControllerApi.getDataPointQaReviewInformationByDataId(testDataPointId1))
            .thenReturn(listOf(rejectedDataPointQaReview))

        documentDeletionService.deleteDocument(testDocumentId)

        verify(mockStorageControllerApi).deleteDocument(any(), any())
        verify(mockDocumentMetaInfoRepository).deleteById(testDocumentId)
    }

    @Test
    fun `check that document with non-rejected dataset reference throws ConflictApiException`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(true)
        whenever(mockStorageControllerApi.getDocumentReferences(any(), any())).thenReturn(
            mapOf(
                "dataPointIds" to emptyList(),
                "datasetIds" to listOf(testDatasetId1),
            ),
        )

        val pendingDatasetQaReview =
            QaReviewResponse(
                dataId = testDatasetId1,
                companyId = "44444444-4444-4444-4444-444444444444",
                companyName = "Great Company Inc.",
                framework = "sfdr",
                reportingPeriod = "2023",
                timestamp = System.currentTimeMillis(),
                qaStatus = QaStatus.Pending,
            )
        whenever(mockQaControllerApi.getQaReviewResponseByDataId(UUID.fromString(testDatasetId1)))
            .thenReturn(pendingDatasetQaReview)

        assertThrows<ConflictApiException> {
            documentDeletionService.deleteDocument(testDocumentId)
        }

        verify(mockStorageControllerApi, never()).deleteDocument(any(), any())
        verify(mockDocumentMetaInfoRepository, never()).deleteById(any())
    }

    @Test
    fun `check that document with data point without QA review throws ConflictApiException`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(true)
        whenever(mockStorageControllerApi.getDocumentReferences(any(), any())).thenReturn(
            mapOf(
                "dataPointIds" to listOf(testDataPointId1),
                "datasetIds" to emptyList(),
            ),
        )

        whenever(mockQaControllerApi.getDataPointQaReviewInformationByDataId(testDataPointId1))
            .thenReturn(emptyList())

        assertThrows<ConflictApiException> {
            documentDeletionService.deleteDocument(testDocumentId)
        }

        verify(mockStorageControllerApi, never()).deleteDocument(any(), any())
        verify(mockDocumentMetaInfoRepository, never()).deleteById(any())
    }

    @Test
    fun `check that non-existent document throws DocumentNotFoundException`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(false)

        assertThrows<DocumentNotFoundException> {
            documentDeletionService.deleteDocument(testDocumentId)
        }

        verify(mockStorageControllerApi, never()).getDocumentReferences(any(), any())
        verify(mockStorageControllerApi, never()).deleteDocument(any(), any())
        verify(mockDocumentMetaInfoRepository, never()).deleteById(any())
    }

    @Test
    fun `check that Internal Storage deletion failure propagates ClientException and database is not touched`() {
        whenever(mockDocumentMetaInfoRepository.existsById(testDocumentId)).thenReturn(true)
        whenever(mockStorageControllerApi.getDocumentReferences(any(), any())).thenReturn(
            mapOf(
                "dataPointIds" to emptyList(),
                "datasetIds" to emptyList(),
            ),
        )
        whenever(mockStorageControllerApi.deleteDocument(any(), any()))
            .thenThrow(ClientException("Internal Storage error"))

        assertThrows<ClientException> {
            documentDeletionService.deleteDocument(testDocumentId)
        }

        verify(mockDocumentMetaInfoRepository, never()).deleteById(any())
    }
}
