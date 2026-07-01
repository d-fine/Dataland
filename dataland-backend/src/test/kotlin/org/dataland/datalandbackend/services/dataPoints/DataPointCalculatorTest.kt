package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class DataPointCalculatorTest {
    private val dataCompositionService = mock<DataCompositionService>()
    private val dataAvailabilityChecker = mock<DataAvailabilityChecker>()
    private val internalStorageAdapter = mock<InternalStorageAdapter>()
    private val specificationService = mock<SpecificationService>()
    private val metaDataManager = mock<DataPointMetaInformationManager>()

    private lateinit var dataPointCalculator: DataPointCalculator

    private val correlationId = "test-correlation-id"
    private val companyId = "test-company-id"
    private val secondaryCompanyId = "other-company-id"
    private val reportingPeriod = "2023"
    private val framework = "test-framework"
    private val frameworkName = "Test Framework"

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

    private fun makeDataPointTypeSpecification(
        dataPointType: String,
        calculationRules: List<CalculationRule> = emptyList(),
    ) = DataPointTypeSpecification(
        dataPointType = IdWithRef(id = dataPointType, ref = ""),
        name = dataPointType,
        businessDefinition = "",
        dataPointBaseType = IdWithRef(id = "numeric", ref = ""),
        usedBy = emptyList(),
        calculationRules = calculationRules,
    )

    private fun makeMetaData(
        dataPointType: String,
        companyId: String = this.companyId,
        reportingPeriod: String = this.reportingPeriod,
        dataPointId: String = "$companyId-$reportingPeriod-$dataPointType",
    ) = DataPointMetaInformationEntity(
        dataPointId = dataPointId,
        companyId = companyId,
        dataPointType = dataPointType,
        reportingPeriod = reportingPeriod,
        uploaderUserId = "test-user-id",
        uploadTime = 0,
        currentlyActive = true,
        qaStatus = QaStatus.Accepted,
    )

    @BeforeEach
    fun setUp() {
        dataPointCalculator =
            DataPointCalculator(
                dataCompositionService, dataAvailabilityChecker,
                internalStorageAdapter, specificationService, metaDataManager,
            )
        doReturn(
            mapOf(
                sourceTypeA to makeDataPointTypeSpecification(sourceTypeA),
                sourceTypeB to makeDataPointTypeSpecification(sourceTypeB),
            ),
        ).whenever(specificationService).getDataPointSpecifications(any())
        doReturn(
            FrameworkSpecification(
                framework = IdWithRef(id = framework, ref = ""),
                name = frameworkName,
                businessDefinition = "",
                schema = "{}",
                referencedReportJsonPath = null,
            ),
        ).whenever(specificationService).getFrameworkSpecification(framework)
    }

    @Test
    fun `check that a data point is correctly calculated when all source data is available`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf, "id-b" to dataPointOne))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA, sourceTypeB)),
                correlationId = correlationId,
            )

        assertTrue(result.containsKey(datasetDimensions))
        val calculatedPoints = result.getValue(datasetDimensions)
        assertEquals(1, calculatedPoints.size)
        val calculatedPoint = calculatedPoints.first()
        assertEquals(targetType, calculatedPoint.dataPointType)
        val calculatedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(calculatedPoint.dataPoint)
        val value = calculatedDataPoint.value
        assertEquals(0, BigDecimal(1.5).compareTo(value!!))
        assertTrue(calculatedDataPoint.comment?.contains("Framework: \"$frameworkName\"") == true)
    }

    @Test
    fun `check that calculation is skipped when source data is missing`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA)),
                correlationId = correlationId,
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that the first applicable rule is used when multiple rules exist`() {
        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(
                targetType to
                    listOf(
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"),
                        CalculationRule(listOf(sourceTypeA), "Identity"),
                    ),
            ),
        ).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA)),
                correlationId = correlationId,
            )

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

        // Rule requires both A and B
        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        // Both returned from storage, but B has no value
        doReturn(mapOf("id-a" to dataPointWithValue, "id-b" to dataPointWithoutValue))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA, sourceTypeB)),
                correlationId = correlationId,
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that dimensions for which no calculation rule exists are omitted from the result`() {
        doReturn(emptyMap<String, Collection<CalculationRule>>()).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(emptyList<String>()).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(emptyMap<String, UploadedDataPoint>())
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA, sourceTypeB)),
                correlationId = correlationId,
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that already deliverable data point types are not calculated again`() {
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(emptyMap<String, Collection<CalculationRule>>()).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(emptyList<String>()).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(emptyMap<String, UploadedDataPoint>())
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(targetType)),
                correlationId = correlationId,
            )

        assertTrue(result.isEmpty())
        verify(dataCompositionService).getAvailableCalculationRules(
            argThat {
                containsAll(listOf(sourceTypeA, sourceTypeB)) && !contains(targetType)
            },
        )
    }

    @Test
    fun `check that multiple dataset dimensions are each handled independently`() {
        val secondCompanyId = secondaryCompanyId
        val secondDimensions = BasicDatasetDimensions(secondCompanyId, framework, reportingPeriod)

        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)
        val secondDataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson, companyId = secondCompanyId)
        val secondDataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson, companyId = secondCompanyId)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
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
        ).whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions, secondDimensions),
                deliverableDataPointTypes =
                    mapOf(
                        datasetDimensions to listOf(sourceTypeA, sourceTypeB),
                        secondDimensions to listOf(sourceTypeA, sourceTypeB),
                    ),
                correlationId = correlationId,
            )
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.getValue(datasetDimensions).first().dataPoint).value

        assertEquals(0, BigDecimal(1.5).compareTo(value!!))
        assertTrue(result.containsKey(datasetDimensions))
        assertTrue(result.containsKey(secondDimensions))
        assertEquals(companyId, result.getValue(datasetDimensions).first().companyId)
        assertEquals(secondCompanyId, result.getValue(secondDimensions).first().companyId)
    }

    @Test
    fun `check that source data from one company is not used for another company`() {
        val secondCompanyId = secondaryCompanyId
        val secondDimensions = BasicDatasetDimensions(secondCompanyId, framework, reportingPeriod)

        val dataPointHalf = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson)
        val dataPointOne = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalf, "id-b" to dataPointOne))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions, secondDimensions),
                deliverableDataPointTypes =
                    mapOf(
                        datasetDimensions to listOf(sourceTypeA, sourceTypeB),
                        secondDimensions to listOf(sourceTypeA, sourceTypeB),
                    ),
                correlationId = correlationId,
            )

        assertEquals(1, result.size)
        assertTrue(result.containsKey(datasetDimensions))
        assertFalse(result.containsKey(secondDimensions))
    }

    @Test
    fun `check that source data from another reporting period is not used`() {
        val dataPointHalfFromAnotherPeriod = makeUploadedDataPoint(sourceTypeA, numericDataPointHalfJson, reportingPeriod = "2024")
        val dataPointOneFromAnotherPeriod = makeUploadedDataPoint(sourceTypeB, numericDataPointOneJson, reportingPeriod = "2024")

        doReturn(
            mapOf<String, Collection<CalculationRule>>(targetType to listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"))),
        ).whenever(dataCompositionService)
            .getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointHalfFromAnotherPeriod, "id-b" to dataPointOneFromAnotherPeriod))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA, sourceTypeB)),
                correlationId = correlationId,
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that a failed calculation rule falls back to the next applicable rule`() {
        val dataPointOne = makeUploadedDataPoint(sourceTypeA, numericDataPointOneJson)
        val dataPointZero = makeUploadedDataPoint(sourceTypeB, zeroNumericDataPointJson)

        doReturn(
            mapOf<String, Collection<CalculationRule>>(
                targetType to
                    listOf(
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Division"),
                        CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum"),
                    ),
            ),
        ).whenever(dataCompositionService).getAvailableCalculationRules(any())
        doReturn(listOf(sourceTypeA, sourceTypeB, targetType)).whenever(dataCompositionService).getRelevantDataPointTypes(framework)
        doReturn(listOf("id-a", "id-b")).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(mapOf("id-a" to dataPointOne, "id-b" to dataPointZero))
            .whenever(internalStorageAdapter)
            .getDataPoints(any(), any())

        val result =
            dataPointCalculator.getCalculatedData(
                datasetDimensions = listOf(datasetDimensions),
                deliverableDataPointTypes =
                    mapOf(datasetDimensions to listOf(sourceTypeA, sourceTypeB)),
                correlationId = correlationId,
            )
        val value = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.getValue(datasetDimensions).first().dataPoint).value

        assertTrue(result.containsKey(datasetDimensions))
        assertEquals(targetType, result.getValue(datasetDimensions).first().dataPointType)
        assertEquals(0, BigDecimal(1.0).compareTo(value!!))
    }

    @Test
    fun `check that active source dimensions are returned only for complete rules in the same base dimensions`() {
        doReturn(
            mapOf(
                targetType to
                    makeDataPointTypeSpecification(
                        targetType,
                        listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum")),
                    ),
            ),
        ).whenever(specificationService).getDataPointSpecifications(listOf(targetType))
        doReturn(
            listOf(
                makeMetaData(sourceTypeA, reportingPeriod = "2022"),
                makeMetaData(sourceTypeB, reportingPeriod = "2022"),
                makeMetaData(sourceTypeA, reportingPeriod = "2023"),
                makeMetaData(sourceTypeB, companyId = secondaryCompanyId, reportingPeriod = "2023"),
            ),
        ).whenever(metaDataManager).getActiveDataPointMetaInformationList(any())

        val result = dataPointCalculator.getActiveSourceDataPointDimensions(listOf(targetType), companyId)

        assertEquals(
            setOf(
                BasicDataPointDimensions(companyId, sourceTypeA, "2022"),
                BasicDataPointDimensions(companyId, sourceTypeB, "2022"),
            ),
            result,
        )
    }

    @Test
    fun `check that active source lookup is constrained to the requested dimensions`() {
        val filter = DataDimensionFilter(companyIds = listOf(companyId), reportingPeriods = listOf("2022"))
        val secondTargetType = "secondCalculatedDataPointType"
        doReturn(
            mapOf(
                targetType to
                    makeDataPointTypeSpecification(
                        targetType,
                        listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum")),
                    ),
                secondTargetType to
                    makeDataPointTypeSpecification(
                        secondTargetType,
                        listOf(CalculationRule(listOf(sourceTypeA), "Identity")),
                    ),
            ),
        ).whenever(specificationService).getDataPointSpecifications(listOf(targetType, secondTargetType))
        doReturn(
            listOf(
                makeMetaData(sourceTypeA, reportingPeriod = "2022"),
                makeMetaData(sourceTypeB, reportingPeriod = "2022"),
            ),
        ).whenever(metaDataManager).getActiveDataPointMetaInformationList(any())

        val result = dataPointCalculator.getActiveSourceDataPointDimensions(listOf(targetType, secondTargetType), filter)

        assertEquals(
            setOf(
                BasicDataPointDimensions(companyId, sourceTypeA, "2022"),
                BasicDataPointDimensions(companyId, sourceTypeB, "2022"),
            ),
            result,
        )
        verify(metaDataManager).getActiveDataPointMetaInformationList(
            argThat {
                companyIds == filter.companyIds &&
                    reportingPeriods == filter.reportingPeriods &&
                    dataTypes?.containsAll(listOf(sourceTypeA, sourceTypeB)) == true
            },
        )
    }

    @Test
    fun `check that active source dimensions are grouped by company and reporting period and deduplicated`() {
        val secondCompanyId = secondaryCompanyId
        val secondTargetType = "secondCalculatedDataPointType"
        doReturn(
            mapOf(
                targetType to
                    makeDataPointTypeSpecification(
                        targetType,
                        listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum")),
                    ),
                secondTargetType to
                    makeDataPointTypeSpecification(
                        secondTargetType,
                        listOf(CalculationRule(listOf(sourceTypeA, sourceTypeB), "Sum")),
                    ),
            ),
        ).whenever(specificationService).getDataPointSpecifications(listOf(targetType, secondTargetType))
        doReturn(
            listOf(
                makeMetaData(sourceTypeA, reportingPeriod = "2022"),
                makeMetaData(sourceTypeB, reportingPeriod = "2022"),
                makeMetaData(sourceTypeA, companyId = secondCompanyId, reportingPeriod = "2024"),
                makeMetaData(sourceTypeB, companyId = secondCompanyId, reportingPeriod = "2024"),
            ),
        ).whenever(metaDataManager).getActiveDataPointMetaInformationList(any())

        val result = dataPointCalculator.getActiveSourceDataPointDimensions(listOf(targetType, secondTargetType), DataDimensionFilter())

        assertEquals(
            setOf(
                BasicDataPointDimensions(companyId, sourceTypeA, "2022"),
                BasicDataPointDimensions(companyId, sourceTypeB, "2022"),
                BasicDataPointDimensions(secondCompanyId, sourceTypeA, "2024"),
                BasicDataPointDimensions(secondCompanyId, sourceTypeB, "2024"),
            ),
            result,
        )
        verify(metaDataManager).getActiveDataPointMetaInformationList(
            argThat {
                dataTypes == listOf(sourceTypeA, sourceTypeB)
            },
        )
    }

    @Test
    fun `check that active source dimension lookup does not infer recursive calculation chains`() {
        val intermediateType = "intermediateCalculatedDataPointType"
        val finalTargetType = "finalCalculatedDataPointType"
        doReturn(
            mapOf(
                finalTargetType to
                    makeDataPointTypeSpecification(
                        finalTargetType,
                        listOf(CalculationRule(listOf(intermediateType), "Identity")),
                    ),
            ),
        ).whenever(specificationService).getDataPointSpecifications(listOf(finalTargetType))
        doReturn(listOf(makeMetaData(sourceTypeA)))
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformationList(any())

        val result = dataPointCalculator.getActiveSourceDataPointDimensions(listOf(finalTargetType), companyId)

        assertTrue(result.isEmpty())
    }
}
