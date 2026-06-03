package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataDeliveryServiceTest {
    private val dataCompositionService = mock<DataCompositionService>()
    private val dataAvailabilityChecker = mock<DataAvailabilityChecker>()
    private val internalStorageAdapter = mock<InternalStorageAdapter>()
    private val datasetAssembler = mock<DatasetAssembler>()
    private val dataPointCalculator = mock<DataPointCalculator>()

    private lateinit var dataDeliveryService: DataDeliveryService

    private val correlationId = "test-correlation-id"
    private val companyId = "test-company-id"
    private val reportingPeriod = "2024"
    private val framework = "test-framework"
    private val directDataPointType = "directDataPointType"
    private val calculatedDataPointType = "calculatedDataPointType"
    private val directDataPointId = "direct-data-point-id"

    private val datasetDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    private val directDataPoint =
        UploadedDataPoint(
            dataPoint = "{\"value\":\"direct\"}",
            dataPointType = directDataPointType,
            companyId = companyId,
            reportingPeriod = reportingPeriod,
        )
    private val calculatedDataPoint =
        UploadedDataPoint(
            dataPoint = "{\"value\":\"calculated\"}",
            dataPointType = calculatedDataPointType,
            companyId = companyId,
            reportingPeriod = reportingPeriod,
        )

    @BeforeEach
    fun setUp() {
        dataDeliveryService =
            DataDeliveryService(
                dataCompositionService = dataCompositionService,
                dataAvailabilityChecker = dataAvailabilityChecker,
                internalStorageAdapter = internalStorageAdapter,
                datasetAssembler = datasetAssembler,
                dataPointCalculator = dataPointCalculator,
            )
        doReturn(listOf(directDataPointType, calculatedDataPointType))
            .whenever(dataCompositionService)
            .getRelevantDataPointTypes(framework)
        doReturn(emptyMap<String, UploadedDataPoint>())
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())
        doReturn(emptyMap<BasicDatasetDimensions, List<UploadedDataPoint>>())
            .whenever(dataPointCalculator)
            .getCalculatedData(any(), any(), any())
    }

    @Test
    fun `check that directly stored data points are assembled`() {
        mockViewableDataPointMetaData(listOf(makeMetaData()))
        doReturn(mapOf(directDataPointId to directDataPoint))
            .whenever(internalStorageAdapter)
            .getDataPoints(listOf(directDataPointId), correlationId)
        doReturn("assembled-direct").whenever(datasetAssembler).assembleSingleDataset(any(), any())

        val result = dataDeliveryService.getAssembledDatasets(listOf(datasetDimensions), correlationId)

        assertEquals(mapOf(datasetDimensions to "assembled-direct"), result)
        verify(datasetAssembler).assembleSingleDataset(listOf(directDataPoint), framework)
    }

    @Test
    fun `check that stored and calculated data points are assembled together`() {
        mockViewableDataPointMetaData(listOf(makeMetaData()))
        doReturn(mapOf(directDataPointId to directDataPoint))
            .whenever(internalStorageAdapter)
            .getDataPoints(listOf(directDataPointId), correlationId)
        doReturn(mapOf(datasetDimensions to listOf(calculatedDataPoint)))
            .whenever(dataPointCalculator)
            .getCalculatedData(any(), any(), any())
        doReturn("assembled-mixed").whenever(datasetAssembler).assembleSingleDataset(any(), any())

        val result = dataDeliveryService.getAssembledDatasets(listOf(datasetDimensions), correlationId)

        assertEquals(mapOf(datasetDimensions to "assembled-mixed"), result)
        verify(datasetAssembler).assembleSingleDataset(listOf(directDataPoint, calculatedDataPoint), framework)
    }

    @Test
    fun `check that calculation only dimensions are assembled`() {
        mockViewableDataPointMetaData(emptyList())
        doReturn(mapOf(datasetDimensions to listOf(calculatedDataPoint)))
            .whenever(dataPointCalculator)
            .getCalculatedData(any(), any(), any())
        doReturn("assembled-calculated").whenever(datasetAssembler).assembleSingleDataset(any(), any())

        val result = dataDeliveryService.getAssembledDatasets(listOf(datasetDimensions), correlationId)

        assertEquals(mapOf(datasetDimensions to "assembled-calculated"), result)
        verify(datasetAssembler).assembleSingleDataset(listOf(calculatedDataPoint), framework)
    }

    @Test
    fun `check that empty direct and calculated inputs are not assembled`() {
        mockViewableDataPointMetaData(emptyList())

        val result = dataDeliveryService.getAssembledDatasets(listOf(datasetDimensions), correlationId)

        assertEquals(emptyMap<BasicDatasetDimensions, String>(), result)
        verify(datasetAssembler, times(0)).assembleSingleDataset(any(), any())
    }

    private fun mockViewableDataPointMetaData(metaData: List<DataPointMetaInformationEntity>) {
        doReturn(mapOf(datasetDimensions to metaData))
            .whenever(dataAvailabilityChecker)
            .getViewableDataPointMetaData(
                argThat<Map<BasicDatasetDimensions, List<BasicDataPointDimensions>>> {
                    keys == setOf(datasetDimensions)
                },
            )
    }

    private fun makeMetaData() =
        DataPointMetaInformationEntity(
            dataPointId = directDataPointId,
            companyId = companyId,
            dataPointType = directDataPointType,
            reportingPeriod = reportingPeriod,
            uploaderUserId = "uploader-user-id",
            uploadTime = 0,
            currentlyActive = true,
            qaStatus = QaStatus.Accepted,
        )
}
