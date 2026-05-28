package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    private val specificationService = mock<SpecificationService>()

    private lateinit var dataPointCalculator: DataPointCalculator

    private val correlationId = "test-correlation-id"
    private val companyId = "test-company-id"
    private val reportingPeriod = "2023"
    private val framework = "test-framework"

    private val sourceTypeA = "sourceDataPointTypeA"
    private val sourceTypeB = "sourceDataPointTypeB"
    private val targetType = "calculatedDataPointType"

    private val numericDataPointHalfJson =
        TestResourceFileReader
            .getJsonString("json/dataPoints/numericDataPointHalf.json")
    private val numericDataPointOneJson =
        TestResourceFileReader
            .getJsonString("./json/dataPoints/numericDataPointOne.json")
    private val dataPointWithoutValueJson =
        TestResourceFileReader
            .getJsonString("./json/dataPoints/dataPointWithoutValue.json")
    private val zeroNumericDataPointJson =
        TestResourceFileReader
            .getJsonString("json/dataPoints/numericDataPointZero.json")

    private val datasetDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    private fun makeUploadedDataPoint(
        type: String,
        dataPointJson: String,
        companyId: String = this.companyId,
        reportingPeriod: String = this.reportingPeriod,
    ) = UploadedDataPoint(
        dataPoint = dataPointJson,
        dataPointType = type,
        companyId = companyId,
        reportingPeriod = reportingPeriod,
    )

    private fun makeStubSpec(dataPointType: String) =
        DataPointTypeSpecification(
            dataPointType = IdWithRef(id = dataPointType, ref = ""),
            name = dataPointType,
            businessDefinition = "",
            dataPointBaseType = IdWithRef(id = "numeric", ref = ""),
            usedBy = emptyList(),
        )

    @BeforeEach
    fun setUp() {
        dataPointCalculator =
            DataPointCalculator(
                dataCompositionService, dataAvailabilityChecker,
                internalStorageAdapter, specificationService,
            )
        doReturn(
            mapOf(
                sourceTypeA to makeStubSpec(sourceTypeA),
                sourceTypeB to makeStubSpec(sourceTypeB),
            ),
        ).whenever(specificationService).getDataPointSpecifications(any())
    }

    @Test
    fun `check that a data point is correctly calculated when all source data is available`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf, "id-b" to dataPointOne))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.containsKey(datasetDimensions))
        val calculatedPoints = result.getValue(datasetDimensions)
        assertEquals(1, calculatedPoints.size)
        val calculatedPoint = calculatedPoints.first()
        assertEquals(targetType, calculatedPoint.dataPointType)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(calculatedPoint.dataPoint).value
        assertEquals(0, BigDecimal(1.5).compareTo(value!!))
    }

    @Test
    fun `check that calculation is skipped when source data is missing`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that the first applicable rule is used when multiple rules exist`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
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
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)

        assertTrue(result.containsKey(datasetDimensions))
        val calculatedPoints = result.getValue(datasetDimensions)
        assertEquals(1, calculatedPoints.size)
        assertEquals(targetType, calculatedPoints.first().dataPointType)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(calculatedPoints.first().dataPoint).value
        assertEquals(0, BigDecimal(0.5).compareTo(value!!))
    }

    @Test
    fun `check that data points with null values are excluded from source data`() {
        val dataPointWithValue = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointWithoutValue = makeUploadedDataPoint(sourceTypeB, dataPointWithoutValueJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        // Rule requires both A and B
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        // Both returned from storage, but B has no value
        doReturn(mapOf("id-a" to dataPointWithValue, "id-b" to dataPointWithoutValue))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that dimensions for which no calculation rule exists are omitted from the result`() {
        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
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

        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)
        val secondDataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson, companyId = secondCompanyId)
        val secondDataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson, companyId = secondCompanyId)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b", "id-second-a", "id-second-b"))
            .whenever(dataAvailabilityChecker)
            .getViewableDataPointIds(any())
        doReturn(
            mapOf(
                "id-a" to dataPointHalf,
                "id-b" to dataPointOne,
                "id-second-a" to secondDataPointHalf,
                "id-second-b" to secondDataPointOne,
            ),
        )
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions, secondDimensions), correlationId)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.getValue(datasetDimensions).first().dataPoint).value

        assertEquals(0, BigDecimal(1.5).compareTo(value!!))
        assertTrue(result.containsKey(datasetDimensions))
        assertTrue(result.containsKey(secondDimensions))
        assertEquals(companyId, result.getValue(datasetDimensions).first().companyId)
        assertEquals(secondCompanyId, result.getValue(secondDimensions).first().companyId)
    }

    @Test
    fun `check that source data from one company is not used for another company`() {
        val secondCompanyId = "other-company-id"
        val secondDimensions = BasicDatasetDimensions(secondCompanyId, framework, reportingPeriod)

        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf, "id-b" to dataPointOne))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions, secondDimensions), correlationId)

        assertEquals(1, result.size)
        assertTrue(result.containsKey(datasetDimensions))
        assertFalse(result.containsKey(secondDimensions))
    }

    @Test
    fun `check that a failed calculation rule falls back to the next applicable rule`() {
        val dataPointOne = makeUploadedDataPoint(sourceTypeA, numericDataPointOneJson)
        val dataPointZero = makeUploadedDataPoint(sourceTypeB, zeroNumericDataPointJson)

        doReturn(listOf(targetType)).whenever(dataAvailabilityChecker).getMissingDataPointTypes(any(), any(), any())
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
        doReturn(mapOf("id-a" to dataPointOne, "id-b" to dataPointZero))
            .whenever(internalStorageAdapter)
            .retrieveDataPointsFromInternalStorage(any(), any())

        val result = dataPointCalculator.getCalculatedData(listOf(datasetDimensions), correlationId)
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.getValue(datasetDimensions).first().dataPoint).value

        assertTrue(result.containsKey(datasetDimensions))
        assertEquals(targetType, result.getValue(datasetDimensions).first().dataPointType)
        assertEquals(0, BigDecimal(1.0).compareTo(value!!))
    }
}
