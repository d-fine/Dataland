package org.dataland.datalandinternalstorage.services

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class DocumentReferenceServiceTest {
    private lateinit var mockEntityManager: EntityManager
    private lateinit var mockDataPointQuery: TypedQuery<String>
    private lateinit var mockDatasetQuery: TypedQuery<String>
    private lateinit var documentReferenceService: DocumentReferenceService

    private val documentId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        mockEntityManager = mock(EntityManager::class.java)
        @Suppress("UNCHECKED_CAST")
        mockDataPointQuery = mock(TypedQuery::class.java) as TypedQuery<String>
        @Suppress("UNCHECKED_CAST")
        mockDatasetQuery = mock(TypedQuery::class.java) as TypedQuery<String>
        documentReferenceService = DocumentReferenceService(mockEntityManager)
    }

    @Test
    fun `check that getDocumentReferences returns data point IDs when document is found in data points`() {
        val expectedDataPointIds = listOf("datapoint-1", "datapoint-2", "datapoint-3")
        val expectedDatasetIds = emptyList<String>()

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentReferenceService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
        verifyQueryExecutions()
    }

    @Test
    fun `check that getDocumentReferences returns dataset IDs when document is found in datasets`() {
        val expectedDataPointIds = emptyList<String>()
        val expectedDatasetIds = listOf("dataset-1", "dataset-2")

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentReferenceService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
        verifyQueryExecutions()
    }

    @Test
    fun `check that getDocumentReferences returns both data point and dataset IDs when document is found in both`() {
        val expectedDataPointIds = listOf("datapoint-1", "datapoint-2")
        val expectedDatasetIds = listOf("dataset-1", "dataset-2", "dataset-3")

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentReferenceService.getDocumentReferences(documentId, correlationId)

        assertEquals(expectedDataPointIds, result["dataPointIds"])
        assertEquals(expectedDatasetIds, result["datasetIds"])
        verifyQueryExecutions()
    }

    @Test
    fun `check that getDocumentReferences returns empty lists when document is not found anywhere`() {
        val expectedDataPointIds = emptyList<String>()
        val expectedDatasetIds = emptyList<String>()

        setupMockQueries(expectedDataPointIds, expectedDatasetIds)

        val result = documentReferenceService.getDocumentReferences(documentId, correlationId)

        assertTrue(result["dataPointIds"]!!.isEmpty())
        assertTrue(result["datasetIds"]!!.isEmpty())
        verifyQueryExecutions()
    }

    private fun setupMockQueries(
        dataPointIds: List<String>,
        datasetIds: List<String>,
    ) {
        `when`(
            mockEntityManager.createQuery(
                "SELECT d.dataPointId FROM DataPointItem d WHERE d.dataPoint LIKE :documentId",
                String::class.java,
            ),
        ).thenReturn(mockDataPointQuery)
        `when`(mockDataPointQuery.setParameter("documentId", "%$documentId%")).thenReturn(mockDataPointQuery)
        `when`(mockDataPointQuery.resultList).thenReturn(dataPointIds)

        `when`(
            mockEntityManager.createQuery(
                "SELECT d.id FROM DataItem d WHERE d.data LIKE :documentId",
                String::class.java,
            ),
        ).thenReturn(mockDatasetQuery)
        `when`(mockDatasetQuery.setParameter("documentId", "%$documentId%")).thenReturn(mockDatasetQuery)
        `when`(mockDatasetQuery.resultList).thenReturn(datasetIds)
    }

    private fun verifyQueryExecutions() {
        verify(mockEntityManager).createQuery(
            "SELECT d.dataPointId FROM DataPointItem d WHERE d.dataPoint LIKE :documentId",
            String::class.java,
        )
        verify(mockDataPointQuery).setParameter("documentId", "%$documentId%")
        verify(mockDataPointQuery).resultList

        verify(mockEntityManager).createQuery(
            "SELECT d.id FROM DataItem d WHERE d.data LIKE :documentId",
            String::class.java,
        )
        verify(mockDatasetQuery).setParameter("documentId", "%$documentId%")
        verify(mockDatasetQuery).resultList
    }
}
