package org.dataland.datalandinternalstorage.services

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class DocumentStorageServiceTest {
    private lateinit var mockEntityManager: EntityManager
    private lateinit var mockDataPointQuery: TypedQuery<String>
    private lateinit var mockDatasetQuery: TypedQuery<String>
    private lateinit var documentStorageService: DocumentStorageService

    private val documentId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()

    private val testDataPointId1 = UUID.randomUUID().toString()
    private val testDataPointId2 = UUID.randomUUID().toString()
    private val testDataPointId3 = UUID.randomUUID().toString()

    private val testDatasetId1 = UUID.randomUUID().toString()
    private val testDatasetId2 = UUID.randomUUID().toString()
    private val testDatasetId3 = UUID.randomUUID().toString()

    companion object {
        private const val DATA_POINT_QUERY = "SELECT d.dataPointId FROM DataPointItem d WHERE d.dataPoint LIKE :documentId"
        private const val DATASET_QUERY = "SELECT d.id FROM DataItem d WHERE d.data LIKE :documentId"
    }

    @BeforeEach
    fun setup() {
        mockEntityManager = mock<EntityManager>()
        mockDataPointQuery = mock<TypedQuery<String>>()
        mockDatasetQuery = mock<TypedQuery<String>>()
        documentStorageService = DocumentStorageService(mockEntityManager, mock())
    }

    @Test
    fun `check that getDocumentReferences returns data point IDs when document is found in data points`() {
        val expectedDataPointIds = listOf(testDataPointId1, testDataPointId2, testDataPointId3)
        val expectedDatasetIds = emptyList<String>()

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
    }

    @Test
    fun `check that getDocumentReferences returns dataset IDs when document is found in datasets`() {
        val expectedDataPointIds = emptyList<String>()
        val expectedDatasetIds = listOf(testDatasetId1, testDatasetId2)

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
    }

    @Test
    fun `check that getDocumentReferences returns both data point and dataset IDs when document is found in both`() {
        val expectedDataPointIds = listOf(testDataPointId1, testDataPointId2)
        val expectedDatasetIds = listOf(testDatasetId1, testDatasetId2, testDatasetId3)

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
    }

    @Test
    fun `check that getDocumentReferences returns empty lists when document is not found anywhere`() {
        val expectedDataPointIds = emptyList<String>()
        val expectedDatasetIds = emptyList<String>()

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertTrue(result["dataPointIds"]!!.isEmpty())
        assertTrue(result["datasetIds"]!!.isEmpty())
    }

    private fun setupMockQueries(
        dataPointIds: List<String>,
        datasetIds: List<String>,
    ) {
        whenever(
            mockEntityManager.createQuery(
                DATA_POINT_QUERY,
                String::class.java,
            ),
        ).thenReturn(mockDataPointQuery)
        whenever(mockDataPointQuery.setParameter("documentId", "%$documentId%")).thenReturn(mockDataPointQuery)
        whenever(mockDataPointQuery.resultList).thenReturn(dataPointIds)

        whenever(
            mockEntityManager.createQuery(
                DATASET_QUERY,
                String::class.java,
            ),
        ).thenReturn(mockDatasetQuery)
        whenever(mockDatasetQuery.setParameter("documentId", "%$documentId%")).thenReturn(mockDatasetQuery)
        whenever(mockDatasetQuery.resultList).thenReturn(datasetIds)
    }
}
