package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.model.DocumentMetaInfoEntity
import org.dataland.documentmanager.openApiClient.model.DocumentType
import org.dataland.documentmanager.openApiClient.model.QaStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class DataDeliveryServiceUtilsTest {
    private val documentControllerApi = mock<DocumentControllerApi>()
    private lateinit var dataDeliveryServiceUtils: DataDeliveryServiceUtils

    private val companyId = "test-company-id"
    private val reportingPeriod = "2024"
    private val framework = "sfdr"
    private val datasetDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    private val enrichableFileReference = "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63"
    private val unresolvableFileReference = "60a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63"
    private val documentName = "ESEFReport"
    private val publicationDate = LocalDate.parse("2023-11-04")

    private fun makeDataPoint(
        dataPoint: String,
        dataPointType: String = "extendedCurrencyEquity",
    ) = UploadedDataPoint(
        dataPoint = dataPoint,
        dataPointType = dataPointType,
        companyId = companyId,
        reportingPeriod = reportingPeriod,
    )

    private fun makeDocumentMetaInfo(
        documentId: String,
        name: String? = documentName,
        date: LocalDate? = publicationDate,
    ) = DocumentMetaInfoEntity(
        documentId = documentId,
        documentType = DocumentType.Pdf,
        companyIds = setOf(companyId),
        uploaderId = "test-uploader-id",
        uploadTime = 0,
        qaStatus = QaStatus.Accepted,
        isViewableByUser = true,
        documentName = name,
        publicationDate = date,
    )

    @BeforeEach
    fun setUp() {
        dataDeliveryServiceUtils = DataDeliveryServiceUtils(documentControllerApi)
    }

    @Test
    fun `check that a data source with a resolvable fileReference is enriched with fileName and publicationDate`() {
        doReturn(mapOf(enrichableFileReference to makeDocumentMetaInfo(enrichableFileReference)))
            .whenever(documentControllerApi)
            .getDocumentMetaInformationBatch(listOf(enrichableFileReference))

        val dataPoint =
            makeDataPoint(
                """{"value":0.5,"dataSource":{"page":"1","fileName":null,"fileReference":"$enrichableFileReference"}}""",
            )

        val result = dataDeliveryServiceUtils.enhanceDataPoints(mapOf("id" to dataPoint), emptyMap())

        val enrichedDataPoint = result.allStoredDataPoints.getValue("id")
        assertTrue(enrichedDataPoint.dataPoint.contains("\"fileName\":\"$documentName\""))
        assertTrue(enrichedDataPoint.dataPoint.contains("\"publicationDate\":\"$publicationDate\""))
    }

    @Test
    fun `check that a data source with an unresolvable fileReference is left unchanged`() {
        doReturn(emptyMap<String, DocumentMetaInfoEntity>())
            .whenever(documentControllerApi)
            .getDocumentMetaInformationBatch(listOf(unresolvableFileReference))

        val dataPoint =
            makeDataPoint(
                """{"value":0.5,"dataSource":{"page":"1","fileName":null,"fileReference":"$unresolvableFileReference"}}""",
            )

        val result = dataDeliveryServiceUtils.enhanceDataPoints(mapOf("id" to dataPoint), emptyMap())

        assertEquals(dataPoint, result.allStoredDataPoints.getValue("id"))
    }

    @Test
    fun `check that data points without any data source are left unchanged and the document manager is not called`() {
        val dataPoint = makeDataPoint("""{"value":0.5}""")

        val result = dataDeliveryServiceUtils.enhanceDataPoints(mapOf("id" to dataPoint), emptyMap())

        assertEquals(dataPoint, result.allStoredDataPoints.getValue("id"))
        verify(documentControllerApi, never()).getDocumentMetaInformationBatch(any())
    }

    @Test
    fun `check that stored and calculated data points sharing a fileReference are both enriched via a single batch call`() {
        doReturn(mapOf(enrichableFileReference to makeDocumentMetaInfo(enrichableFileReference)))
            .whenever(documentControllerApi)
            .getDocumentMetaInformationBatch(listOf(enrichableFileReference))

        val storedDataPoint =
            makeDataPoint(
                """{"value":0.5,"dataSource":{"page":"1","fileName":null,"fileReference":"$enrichableFileReference"}}""",
            )
        val calculatedDataPoint =
            makeDataPoint(
                """{"value":0.7,"dataSource":{"page":"2","fileName":null,"fileReference":"$enrichableFileReference"}}""",
            )

        val result =
            dataDeliveryServiceUtils.enhanceDataPoints(
                mapOf("id" to storedDataPoint),
                mapOf(datasetDimensions to listOf(calculatedDataPoint)),
            )

        assertTrue(
            result.allStoredDataPoints
                .getValue("id")
                .dataPoint
                .contains("\"fileName\":\"$documentName\""),
        )
        assertTrue(
            result.calculatedData
                .getValue(datasetDimensions)
                .first()
                .dataPoint
                .contains("\"fileName\":\"$documentName\""),
        )
        verify(documentControllerApi, org.mockito.kotlin.times(1)).getDocumentMetaInformationBatch(listOf(enrichableFileReference))
    }

    @Test
    fun `check that nested data sources within a single data point are all enriched`() {
        doReturn(mapOf(enrichableFileReference to makeDocumentMetaInfo(enrichableFileReference)))
            .whenever(documentControllerApi)
            .getDocumentMetaInformationBatch(listOf(enrichableFileReference))

        val dataPoint =
            makeDataPoint(
                """
                {
                  "eligibleShare": {
                    "value": 0.5,
                    "dataSource": {"page": "1", "fileName": null, "fileReference": "$enrichableFileReference"}
                  },
                  "alignedShare": {
                    "value": 0.7,
                    "dataSource": {"page": "2", "fileName": null, "fileReference": "$enrichableFileReference"}
                  }
                }
                """.trimIndent(),
            )

        val result = dataDeliveryServiceUtils.enhanceDataPoints(mapOf("id" to dataPoint), emptyMap())

        val enrichedContent = result.allStoredDataPoints.getValue("id").dataPoint
        assertEquals(2, Regex("\"fileName\":\"$documentName\"").findAll(enrichedContent).count())
    }

    @Test
    fun `check that a missing documentName or publicationDate does not overwrite the existing null values`() {
        doReturn(mapOf(enrichableFileReference to makeDocumentMetaInfo(enrichableFileReference, name = null, date = null)))
            .whenever(documentControllerApi)
            .getDocumentMetaInformationBatch(listOf(enrichableFileReference))

        val dataPoint =
            makeDataPoint(
                """{"value":0.5,"dataSource":{"page":"1","fileName":null,"fileReference":"$enrichableFileReference"}}""",
            )

        val result = dataDeliveryServiceUtils.enhanceDataPoints(mapOf("id" to dataPoint), emptyMap())

        assertFalse(
            result.allStoredDataPoints
                .getValue("id")
                .dataPoint
                .contains("\"fileName\":\"$documentName\""),
        )
    }
}
