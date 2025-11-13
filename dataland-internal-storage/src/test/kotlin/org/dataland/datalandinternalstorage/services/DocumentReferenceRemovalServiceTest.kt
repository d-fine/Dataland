package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DocumentReferenceRemovalServiceTest {
    private val mockBlobItemRepository: BlobItemRepository = mock<BlobItemRepository>()
    private val mockDataItemRepository: DataItemRepository = mock<DataItemRepository>()
    private val mockDataPointItemRepository: DataPointItemRepository = mock<DataPointItemRepository>()
    private val documentReferenceRemovalService: DocumentReferenceRemovalService =
        DocumentReferenceRemovalService(
            mockBlobItemRepository,
            mockDataItemRepository,
            mockDataPointItemRepository,
        )

    private val documentId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()

    @AfterEach
    fun resetMocks() {
        reset(mockBlobItemRepository, mockDataItemRepository, mockDataPointItemRepository)
    }

    @Test
    fun `check that getDocumentReferences returns data point IDs when document is found in data points`() {
        val dataPointIds = List(3) { UUID.randomUUID().toString() }
        val dataPointsWithDocument = dataPointIds.map { createDataPointItem(it, documentId) }

        setupMockRepositories(dataPointsWithDocument, emptyList())

        val result = documentReferenceRemovalService.getDocumentReferences(documentId, correlationId)

        assertEquals(dataPointIds.toSet(), result.dataPointIds)
        assertEquals(emptySet<String>(), result.datasetIds)
    }

    @Test
    fun `check that getDocumentReferences returns dataset IDs when document is found in datasets`() {
        val datasetIds = List(3) { UUID.randomUUID().toString() }
        val datasetsWithDocument = datasetIds.map { createDataItem(it, documentId) }

        setupMockRepositories(emptyList(), datasetsWithDocument)

        val result = documentReferenceRemovalService.getDocumentReferences(documentId, correlationId)

        assertEquals(emptySet<String>(), result.dataPointIds)
        assertEquals(datasetIds.toSet(), result.datasetIds)
    }

    @Test
    fun `check that getDocumentReferences returns both data point and dataset IDs when document is found in both`() {
        val dataPointIds = List(3) { UUID.randomUUID().toString() }
        val datasetIds = List(3) { UUID.randomUUID().toString() }

        val dataPointsWithDocument = dataPointIds.map { createDataPointItem(it, documentId) }
        val datasetsWithDocument = datasetIds.map { createDataItem(it, documentId) }

        setupMockRepositories(dataPointsWithDocument, datasetsWithDocument)

        val result = documentReferenceRemovalService.getDocumentReferences(documentId, correlationId)

        assertEquals(dataPointIds.toSet(), result.dataPointIds)
        assertEquals(datasetIds.toSet(), result.datasetIds)
    }

    @Test
    fun `check that getDocumentReferences returns empty lists when document is not found anywhere`() {
        setupMockRepositories(emptyList(), emptyList())

        val result = documentReferenceRemovalService.getDocumentReferences(documentId, correlationId)

        assertEquals(emptySet<String>(), result.dataPointIds)
        assertEquals(emptySet<String>(), result.datasetIds)
    }

    @Test
    fun `check that deleteDocument calls repository deleteById with correct documentId`() {
        setupMockRepositories(emptyList(), emptyList())

        documentReferenceRemovalService.deleteDocument(documentId, correlationId)

        verify(mockBlobItemRepository).deleteById(documentId)
    }

    @Test
    fun `check that nullification handles LkSG dataset correctly`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val lksgDataset = createLksgDataset(lksgDatasetId, riskManagementSystemDocumentReference = documentId)

        whenever(mockDataPointItemRepository.findByDataPointContaining(documentId)).thenReturn(emptyList())
        whenever(mockDataItemRepository.findByDataContaining(documentId)).thenReturn(listOf(lksgDataset))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDataset))

        documentReferenceRemovalService.deleteDocument(documentId, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val wrappedDataset = defaultObjectMapper.readTree(defaultObjectMapper.readTree(savedData).asText())
        val serializedDatasetData = defaultObjectMapper.readTree(wrappedDataset.get("data").asText())

        val dataSource = serializedDatasetData.at("/governance/riskManagementOwnOperations/riskManagementSystem/dataSource")
        assert(dataSource.isNull) { "dataSource should be null after nullification" }
    }

    @Test
    fun `check that deleting one document preserves other document references in same dataset`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val documentIdToBeDeleted = UUID.randomUUID().toString()
        val documentIdToBeKept = UUID.randomUUID().toString()

        val lksgDatasetWithTwoDocuments =
            createLksgDataset(
                lksgDatasetId, riskManagementSystemDocumentReference = documentIdToBeDeleted,
                riskManagementProcessDocumentReference = documentIdToBeKept,
            )

        whenever(mockDataPointItemRepository.findByDataPointContaining(documentIdToBeDeleted)).thenReturn(emptyList())
        whenever(mockDataItemRepository.findByDataContaining(documentIdToBeDeleted))
            .thenReturn(listOf(lksgDatasetWithTwoDocuments))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDatasetWithTwoDocuments))

        documentReferenceRemovalService.deleteDocument(documentIdToBeDeleted, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val wrappedDataset = defaultObjectMapper.readTree(defaultObjectMapper.readTree(savedData).asText())
        val serializedDatasetData = defaultObjectMapper.readTree(wrappedDataset.get("data").asText())

        val removedDataSource = serializedDatasetData.at("/governance/riskManagementOwnOperations/riskManagementSystem/dataSource")
        assert(removedDataSource.isNull) { "First document reference should be null after deletion" }

        val preservedDataSource = serializedDatasetData.at("/governance/riskManagementOwnOperations/riskManagementProcess/dataSource")
        assert(!preservedDataSource.isNull) { "Preserved document reference should not be null" }
        assert(preservedDataSource.has("fileReference")) { "Preserved document reference should have fileReference field" }
        assertEquals(
            documentIdToBeKept,
            preservedDataSource.get("fileReference").asText(),
            "Second document fileReference should be preserved",
        )

        verify(mockBlobItemRepository).deleteById(documentIdToBeDeleted)
    }

    @Test
    fun `check that attachment structure is cleaned up when dataSource is nullified`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val lksgDatasetWithAttachment = createLksgDataset(lksgDatasetId, lksgAttachmentDoc = documentId)

        whenever(mockDataPointItemRepository.findByDataPointContaining(documentId)).thenReturn(emptyList())
        whenever(mockDataItemRepository.findByDataContaining(documentId)).thenReturn(listOf(lksgDatasetWithAttachment))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDatasetWithAttachment))

        documentReferenceRemovalService.deleteDocument(documentId, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val wrappedDataset = defaultObjectMapper.readTree(defaultObjectMapper.readTree(savedData).asText())
        val serializedDatasetData = defaultObjectMapper.readTree(wrappedDataset.get("data").asText())

        val attachmentField = serializedDatasetData.at("/attachment/attachment/attachment")
        assert(attachmentField.isNull) { "attachment.attachment.attachment should be null after document deletion" }
    }

    private fun createLksgDataset(
        datasetId: String,
        riskManagementSystemDocumentReference: String? = null,
        riskManagementProcessDocumentReference: String? = null,
        lksgAttachmentDoc: String? = null,
    ): DataItem {
        val riskManagementSystemDataSource =
            if (riskManagementSystemDocumentReference != null) {
                """{"fileName":"TestDoc1","fileReference":"$riskManagementSystemDocumentReference","publicationDate":null}"""
            } else {
                "null"
            }

        val riskManagementProcessDataSource =
            if (riskManagementProcessDocumentReference != null) {
                """{"fileName":"TestDoc2","fileReference":"$riskManagementProcessDocumentReference","publicationDate":null}"""
            } else {
                "null"
            }

        val lksgAttachmentDataSource =
            if (lksgAttachmentDoc != null) {
                """{"value":"Yes","dataSource":{"fileName":"AttachmentDoc","fileReference":"$lksgAttachmentDoc"}}"""
            } else {
                "null"
            }

        val lksgData =
            """
            {"governance":{"riskManagementOwnOperations":{
            "riskManagementSystem":{"value":"Yes","dataSource":$riskManagementSystemDataSource},
            "riskManagementProcess":{"value":"Yes","dataSource":$riskManagementProcessDataSource}}},
            "attachment":{"attachment":{"attachment":$lksgAttachmentDataSource}}}
            """.trimIndent().replace("\n", "")

        val lksgDataEntry =
            defaultObjectMapper.writeValueAsString(
                mapOf(
                    "companyId" to UUID.randomUUID().toString(),
                    "dataType" to "lksg",
                    "reportingPeriod" to "2025",
                    "data" to lksgData,
                ),
            )
        return DataItem(
            id = datasetId,
            data = defaultObjectMapper.writeValueAsString(lksgDataEntry),
        )
    }

    private fun setupMockRepositories(
        dataPoints: List<DataPointItem>,
        datasets: List<DataItem>,
    ) {
        whenever(mockDataPointItemRepository.findByDataPointContaining(documentId)).thenReturn(dataPoints)
        whenever(mockDataItemRepository.findByDataContaining(documentId)).thenReturn(datasets)
    }

    private fun createDataPointItem(
        dataPointId: String,
        documentId: String,
    ): DataPointItem {
        val businessData = """{"dataSource":{"fileReference":"$documentId"}}"""
        return DataPointItem(
            dataPointId = dataPointId,
            companyId = "test-company-id",
            reportingPeriod = "2021",
            dataPointType = "testType",
            dataPoint = defaultObjectMapper.writeValueAsString(businessData),
        )
    }

    private fun createDataItem(
        datasetId: String,
        documentId: String,
    ): DataItem {
        val innerData = """{"general":{"referencedReports":{"report1":{"fileReference":"$documentId"}}}}"""
        val outerData =
            defaultObjectMapper.writeValueAsString(
                mapOf(
                    "companyId" to UUID.randomUUID().toString(),
                    "dataType" to "eutaxonomy-non-financials",
                    "reportingPeriod" to "2021",
                    "data" to innerData,
                ),
            )
        return DataItem(
            id = datasetId,
            data = defaultObjectMapper.writeValueAsString(outerData),
        )
    }
}
