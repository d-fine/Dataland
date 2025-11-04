package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DocumentStorageServiceTest {
    private lateinit var mockBlobItemRepository: BlobItemRepository
    private lateinit var mockDataItemRepository: DataItemRepository
    private lateinit var mockDataPointItemRepository: DataPointItemRepository
    private lateinit var objectMapper: ObjectMapper
    private lateinit var documentStorageService: DocumentStorageService

    private val documentId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()

    private val dataPointIdWithDocumentReference1 = UUID.randomUUID().toString()
    private val dataPointIdWithDocumentReference2 = UUID.randomUUID().toString()
    private val dataPointIdWithDocumentReference3 = UUID.randomUUID().toString()

    private val datasetIdWithDocumentReference1 = UUID.randomUUID().toString()
    private val datasetIdWithDocumentReference2 = UUID.randomUUID().toString()
    private val datasetIdWithDocumentReference3 = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        mockBlobItemRepository = mock<BlobItemRepository>()
        mockDataItemRepository = mock<DataItemRepository>()
        mockDataPointItemRepository = mock<DataPointItemRepository>()
        objectMapper = ObjectMapper()
        documentStorageService =
            DocumentStorageService(
                mockBlobItemRepository,
                mockDataItemRepository,
                mockDataPointItemRepository,
                objectMapper,
            )
    }

    @Test
    fun `check that getDocumentReferences returns data point IDs when document is found in data points`() {
        val dataPointsWithDocument =
            listOf(
                createDataPointItem(dataPointIdWithDocumentReference1, documentId),
                createDataPointItem(dataPointIdWithDocumentReference2, documentId),
                createDataPointItem(dataPointIdWithDocumentReference3, documentId),
            )
        val datasetsWithoutDocument = emptyList<DataItem>()

        setupMockRepositories(dataPointsWithDocument, datasetsWithoutDocument)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(
            listOf(dataPointIdWithDocumentReference1, dataPointIdWithDocumentReference2, dataPointIdWithDocumentReference3),
            result["dataPointIds"],
        )
        assertEquals(emptyList<String>(), result["datasetIds"])
    }

    @Test
    fun `check that getDocumentReferences returns dataset IDs when document is found in datasets`() {
        val dataPointsWithoutDocument = emptyList<DataPointItem>()
        val datasetsWithDocument =
            listOf(
                createDataItem(datasetIdWithDocumentReference1, documentId),
                createDataItem(datasetIdWithDocumentReference2, documentId),
            )

        setupMockRepositories(dataPointsWithoutDocument, datasetsWithDocument)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(emptyList<String>(), result["dataPointIds"])
        assertEquals(listOf(datasetIdWithDocumentReference1, datasetIdWithDocumentReference2), result["datasetIds"])
    }

    @Test
    fun `check that getDocumentReferences returns both data point and dataset IDs when document is found in both`() {
        val dataPointsWithDocument =
            listOf(
                createDataPointItem(dataPointIdWithDocumentReference1, documentId),
                createDataPointItem(dataPointIdWithDocumentReference2, documentId),
            )
        val datasetsWithDocument =
            listOf(
                createDataItem(datasetIdWithDocumentReference1, documentId),
                createDataItem(datasetIdWithDocumentReference2, documentId),
                createDataItem(datasetIdWithDocumentReference3, documentId),
            )

        setupMockRepositories(dataPointsWithDocument, datasetsWithDocument)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(listOf(dataPointIdWithDocumentReference1, dataPointIdWithDocumentReference2), result["dataPointIds"])
        assertEquals(
            listOf(datasetIdWithDocumentReference1, datasetIdWithDocumentReference2, datasetIdWithDocumentReference3),
            result["datasetIds"],
        )
    }

    @Test
    fun `check that getDocumentReferences returns empty lists when document is not found anywhere`() {
        val dataPointsWithoutDocument =
            listOf(
                createDataPointItem(dataPointIdWithDocumentReference1, "otherDocumentId"),
            )
        val datasetsWithoutDocument =
            listOf(
                createDataItem(datasetIdWithDocumentReference1, "anotherDocumentId"),
            )

        setupMockRepositories(dataPointsWithoutDocument, datasetsWithoutDocument)

        val result = documentStorageService.getDocumentReferences(documentId, correlationId)

        assertEquals(emptyList<String>(), result["dataPointIds"])
        assertEquals(emptyList<String>(), result["datasetIds"])
    }

    @Test
    fun `check that deleteDocument calls repository deleteById with correct documentId`() {
        setupMockRepositories(emptyList(), emptyList())

        documentStorageService.deleteDocument(documentId, correlationId)

        verify(mockBlobItemRepository).deleteById(documentId)
    }

    @Test
    fun `check that nullification handles LkSG dataset correctly`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val lksgDataset = createLksgDataset(lksgDatasetId, governanceDoc1 = documentId)

        whenever(mockDataPointItemRepository.findAll()).thenReturn(emptyList())
        whenever(mockDataItemRepository.findAll()).thenReturn(listOf(lksgDataset))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDataset))

        documentStorageService.deleteDocument(documentId, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val root = objectMapper.readTree(savedData)
        val outerJson = if (root.isTextual) objectMapper.readTree(root.asText()) else root
        val innerData = objectMapper.readTree(outerJson.get("data").asText())

        val dataSource = innerData.at("/governance/riskManagementOwnOperations/riskManagementSystem/dataSource")
        assert(dataSource.isNull) { "dataSource should be null after nullification" }
    }

    @Test
    fun `check that attachment field is nullified when document is deleted from LkSG dataset`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val lksgDatasetWithAttachment = createLksgDataset(lksgDatasetId, attachmentDoc = documentId)

        whenever(mockDataPointItemRepository.findAll()).thenReturn(emptyList())
        whenever(mockDataItemRepository.findAll()).thenReturn(listOf(lksgDatasetWithAttachment))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDatasetWithAttachment))

        documentStorageService.deleteDocument(documentId, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val root = objectMapper.readTree(savedData)
        val outerJson = if (root.isTextual) objectMapper.readTree(root.asText()) else root
        val innerData = objectMapper.readTree(outerJson.get("data").asText())

        val attachmentField = innerData.at("/attachment/attachment/attachment")
        assert(attachmentField.isNull) { "attachment.attachment.attachment should be null after document deletion" }
    }

    @Test
    fun `check that deleting one document preserves other document references in same dataset`() {
        val lksgDatasetId = UUID.randomUUID().toString()
        val document1Id = UUID.randomUUID().toString()
        val document2Id = UUID.randomUUID().toString()

        val lksgDatasetWithTwoDocuments = createLksgDataset(lksgDatasetId, governanceDoc1 = document1Id, governanceDoc2 = document2Id)

        whenever(mockDataPointItemRepository.findAll()).thenReturn(emptyList())
        whenever(mockDataItemRepository.findAll()).thenReturn(listOf(lksgDatasetWithTwoDocuments))
        whenever(mockDataItemRepository.findById(lksgDatasetId)).thenReturn(java.util.Optional.of(lksgDatasetWithTwoDocuments))

        documentStorageService.deleteDocument(document1Id, correlationId)

        val savedDataset = org.mockito.kotlin.argumentCaptor<DataItem>()
        verify(mockDataItemRepository).save(savedDataset.capture())

        val savedData = savedDataset.firstValue.data
        val root = objectMapper.readTree(savedData)
        val outerJson = if (root.isTextual) objectMapper.readTree(root.asText()) else root
        val innerData = objectMapper.readTree(outerJson.get("data").asText())

        val dataSource1 = innerData.at("/governance/riskManagementOwnOperations/riskManagementSystem/dataSource")
        assert(dataSource1.isNull) { "First document reference should be null after deletion" }

        val dataSource2 = innerData.at("/governance/riskManagementOwnOperations/riskManagementProcess/dataSource")
        assert(!dataSource2.isNull && dataSource2.has("fileReference")) { "Second document reference should still exist" }
        assertEquals(
            document2Id,
            dataSource2.get("fileReference").asText(),
            "Second document fileReference should be preserved",
        )

        verify(mockBlobItemRepository).deleteById(document1Id)
    }

    private fun createLksgDataset(
        datasetId: String,
        governanceDoc1: String? = null,
        governanceDoc2: String? = null,
        attachmentDoc: String? = null,
    ): DataItem {
        val doc1Json =
            if (governanceDoc1 != null) {
                """{"fileName":"TestDoc1","fileReference":"$governanceDoc1","publicationDate":null}"""
            } else {
                "null"
            }

        val doc2Json =
            if (governanceDoc2 != null) {
                """{"fileName":"TestDoc2","fileReference":"$governanceDoc2","publicationDate":null}"""
            } else {
                "null"
            }

        val attachmentJson =
            if (attachmentDoc != null) {
                """{"value":"Yes","dataSource":{"fileName":"AttachmentDoc","fileReference":"$attachmentDoc"}}"""
            } else {
                "null"
            }

        val actualData =
            """
            {"governance":{"riskManagementOwnOperations":{"riskManagementSystem":{"value":"Yes","dataSource":$doc1Json},
            "riskManagementProcess":{"value":"Yes","dataSource":$doc2Json}}},
            "attachment":{"attachment":{"attachment":$attachmentJson}}}
            """.trimIndent().replace("\n", "")

        val datasetJson =
            objectMapper.writeValueAsString(
                mapOf(
                    "companyId" to UUID.randomUUID().toString(),
                    "dataType" to "lksg",
                    "reportingPeriod" to "2025",
                    "data" to actualData,
                ),
            )
        return DataItem(
            id = datasetId,
            data = datasetJson,
        )
    }

    private fun setupMockRepositories(
        dataPoints: List<DataPointItem>,
        datasets: List<DataItem>,
    ) {
        whenever(mockDataPointItemRepository.findAll()).thenReturn(dataPoints)
        whenever(mockDataItemRepository.findAll()).thenReturn(datasets)
    }

    private fun createDataPointItem(
        dataPointId: String,
        documentId: String,
    ): DataPointItem {
        val innerData = """{"dataSource":{"fileReference":"$documentId"}}"""
        val outerData =
            objectMapper.writeValueAsString(
                mapOf(
                    "companyId" to UUID.randomUUID().toString(),
                    "dataPointType" to "testType",
                    "reportingPeriod" to "2021",
                    "data" to innerData,
                ),
            )
        return DataPointItem(
            dataPointId = dataPointId,
            companyId = UUID.randomUUID().toString(),
            reportingPeriod = "2021",
            dataPointType = "testType",
            dataPoint = outerData,
        )
    }

    private fun createDataItem(
        datasetId: String,
        documentId: String,
    ): DataItem {
        val innerData = """{"general":{"referencedReports":{"report1":{"fileReference":"$documentId"}}}}"""
        val outerData =
            objectMapper.writeValueAsString(
                mapOf(
                    "companyId" to UUID.randomUUID().toString(),
                    "dataType" to "eutaxonomy-non-financials",
                    "reportingPeriod" to "2021",
                    "data" to innerData,
                ),
            )
        return DataItem(
            id = datasetId,
            data = outerData,
        )
    }
}
