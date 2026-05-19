package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.CalculationRule
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class DataPointCalculatorTest {
    private val dataCompositionService = mock<DataCompositionService>()
    private val dataAvailabilityChecker = mock<DataAvailabilityChecker>()
    private val internalStorageAdapter = mock<InternalStorageAdapter>()

    private lateinit var dataPointCalculator: DataPointCalculator

    private val correlationId = "test-correlation-id"
    private val companyId = "test-company-id"
    private val reportingPeriod = "2023"
    private val framework = "test-framework"

    private val sourceTypeA = "sourceDataPointTypeA"
    private val sourceTypeB = "sourceDataPointTypeB"
    private val targetType = "calculatedDataPointType"

    private val numericDataPointJson =
        TestResourceFileReader
            .getJsonString("./json/dataPoints/numericDataPointWithExtendedDocumentReference.json")
    private val anotherNumericDataPointJson =
        TestResourceFileReader
            .getJsonString("./json/dataPoints/anotherNumericDataPointForTestingTransformations.json")
    private val dataPointWithoutValueJson =
        TestResourceFileReader
            .getJsonString("./json/dataPoints/dataPointWithoutValue.json")

    private val datasetDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    private fun makeUploadedDataPoint(
        type: String,
        dataPointJson: String,
    ) = UploadedDataPoint(
        dataPoint = dataPointJson,
        dataPointType = type,
        companyId = companyId,
        reportingPeriod = reportingPeriod,
    )

    @BeforeEach
    fun setUp() {
        dataPointCalculator = DataPointCalculator(dataCompositionService, dataAvailabilityChecker, internalStorageAdapter)
    }

    @Test
    fun `check that a data point is correctly calculated when all source data is available`() {
        val sourceA = makeUploadedDataPoint(sourceTypeA, numericDataPointJson)
        val sourceB = makeUploadedDataPoint(sourceTypeB, anotherNumericDataPointJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to sourceA, "id-b" to sourceB))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.containsKey(datasetDimensions))
        val calculatedPoints = result.getValue(datasetDimensions)
        assertEquals(1, calculatedPoints.size)
        val calculatedPoint = calculatedPoints.first()
        assertEquals(targetType, calculatedPoint.dataPointType)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(calculatedPoint.dataPoint).value
        assertEquals(BigDecimal.valueOf(2.0), value)
    }

    @Test
    fun `check that calculation is skipped when source data is missing`() {
        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        // Only source A is available, source B is missing
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to makeUploadedDataPoint(sourceTypeA, numericDataPointJson)))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that the first applicable rule is used when multiple rules exist`() {
        val sourceA = makeUploadedDataPoint(sourceTypeA, numericDataPointJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        // First rule requires both A and B, second rule only requires A (identity)
        doReturn(
            mapOf<String, Collection<CalculationRule>>(
                targetType to
                    listOf(
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"),
                        CalculationRule(listOf(sourceTypeA), "Identity"),
                    ),
            ),
        ).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        // Only source A is available
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to sourceA))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.containsKey(datasetDimensions))
        val calculatedPoints = result.getValue(datasetDimensions)
        assertEquals(1, calculatedPoints.size)
        assertEquals(targetType, calculatedPoints.first().dataPointType)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(calculatedPoints.first().dataPoint).value
        assertEquals(BigDecimal.valueOf(0.5), value)
    }

    @Test
    fun `check that data points with null values are excluded from source data`() {
        val sourceWithValue = makeUploadedDataPoint(sourceTypeA, numericDataPointJson)
        val sourceWithoutValue = makeUploadedDataPoint(sourceTypeB, dataPointWithoutValueJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        // Rule requires both A and B
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        // Both returned from storage, but B has no value
        doReturn(mapOf("id-a" to sourceWithValue, "id-b" to sourceWithoutValue))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        // Calculation should be skipped because sourceTypeB has no value and is filtered out
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that dimensions for which no calculation is possible are omitted from the result`() {
        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        // No calculation rules defined for the missing type
        doReturn(emptyMap<String, Collection<CalculationRule>>()).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(emptyList<String>()).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(emptyMap<String, UploadedDataPoint>())
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that multiple dataset dimensions are each handled independently`() {
        val secondCompanyId = "other-company-id"
        val secondDimensions = BasicDatasetDimensions(secondCompanyId, framework, reportingPeriod)

        val sourceA = makeUploadedDataPoint(sourceTypeA, numericDataPointJson)
        val sourceB = makeUploadedDataPoint(sourceTypeB, anotherNumericDataPointJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to sourceA, "id-b" to sourceB))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions, secondDimensions), correlationId)

        assertEquals(2, result.size)
        assertTrue(result.containsKey(datasetDimensions))
        assertTrue(result.containsKey(secondDimensions))
    }

    @Test
    fun `check that a failed calculation rule falls back to the next applicable rule`() {
        val sourceA = makeUploadedDataPoint(sourceTypeA, numericDataPointJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        // First rule uses division by zero (will throw IllegalArgumentException), second rule sums
        val divisionJson = """{"value": 0.0}"""
        val zeroDivisor = makeUploadedDataPoint(sourceTypeB, divisionJson)
        doReturn(
            mapOf<String, Collection<CalculationRule>>(
                targetType to
                    listOf(
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Division"),
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"),
                    ),
            ),
        ).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to sourceA, "id-b" to zeroDivisor))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        // Division by zero should be skipped, Sum should succeed
        assertTrue(result.containsKey(datasetDimensions))
        assertEquals(targetType, result.getValue(datasetDimensions).first().dataPointType)
    }
}
