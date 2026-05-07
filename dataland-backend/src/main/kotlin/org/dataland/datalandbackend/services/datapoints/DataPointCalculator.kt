package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Service to calculate data points based on other data points in case there is no direct data available.
 */
@Service("DataPointCalculator")
class DataPointCalculator
    @Autowired
    constructor(
        private val dataCompositionService: DataCompositionService,
        private val dataAvailabilityChecker: DataAvailabilityChecker,
        private val internalStorageAdapter: InternalStorageAdapter,
    ) {
        private fun removeDataPointsWithoutValue(dataPoints: Collection<UploadedDataPoint>): Collection<UploadedDataPoint> {
            val result = mutableListOf<UploadedDataPoint>()
            dataPoints.forEach { uploadedDataPoint ->
                try {
                    // ToDo flexibler den cast gestalten
                    val castedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(uploadedDataPoint.dataPoint)
                    if (castedDataPoint.value != null) {
                        result.add(uploadedDataPoint)
                    }
                } catch (ignore: Exception) {
                    // Skipping data point as it cannot be cast into the expected type
                }
            }
            return result
        }

        private fun getAvailableSourceData(
            dataPointTypes: Collection<DataPointType>,
            reportingPeriod: String,
            companyId: String,
            correlationId: String,
        ): Collection<UploadedDataPoint> {
            val allDimensions =
                dataPointTypes.map {
                    BasicDataPointDimensions(reportingPeriod = reportingPeriod, dataPointType = it, companyId = companyId)
                }
            val allAvailableIds = dataAvailabilityChecker.getViewableDataPointIds(allDimensions)
            val allStoredDataPoints =
                internalStorageAdapter
                    .retrieveDataPointsFromInternalStorage(dataPointIds = allAvailableIds, correlationId = correlationId)
            return removeDataPointsWithoutValue(allStoredDataPoints.values)
        }

        private fun calculateDataPoints(
            dataPointTypes: Collection<String>,
            companyId: String,
            reportingPeriod: String,
            correlationId: String,
        ): List<UploadedDataPoint> {
            val potentialCalculations = dataCompositionService.getAvailableCalculationRules(dataPointTypes)
            val allSourceTypes = potentialCalculations.values.flatten().flatMap { it.inputs }
            val allSourceData = getAvailableSourceData(allSourceTypes, reportingPeriod, companyId, correlationId)
            val allAvailableSourceTypes = allSourceData.map { it.dataPointType }
            val result = mutableListOf<UploadedDataPoint>()
            potentialCalculations.forEach { (dataPointType, calculationRules) ->
                calculationRules.forEach { calculationRule ->
                    if (allAvailableSourceTypes.containsAll(calculationRule.inputs)) {
                        val targetDimensions =
                            BasicDataPointDimensions(
                                companyId = companyId,
                                dataPointType = dataPointType,
                                reportingPeriod = reportingPeriod,
                            )
                        val orderedInputs =
                            calculationRule.inputs.map { sourceType ->
                                allSourceData.single { it.dataPointType == sourceType }
                            }
                        result
                            .add(
                                calculateSingleDataPoint(
                                    inputs = orderedInputs,
                                    method = calculationRule.calculationMethod,
                                    dataPointDimensions = targetDimensions,
                                ),
                            )
                        return@forEach
                    }
                }
            }
            return result
        }

        private fun calculateSingleDataPoint(
            inputs: Collection<UploadedDataPoint>,
            method: String,
            dataPointDimensions: BasicDataPointDimensions,
        ): UploadedDataPoint =
            UploadedDataPoint(
                dataPoint =
                    applyTransformation(
                        inputs = inputs.map { it.dataPoint },
                        method = method,
                    ),
                dataPointType = dataPointDimensions.dataPointType,
                reportingPeriod = dataPointDimensions.reportingPeriod,
                companyId = dataPointDimensions.companyId,
            )

        fun getCalculatedData(
            datasetDimensions: Collection<BasicDatasetDimensions>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, List<UploadedDataPoint>> {
            val calculatedData = mutableMapOf<BasicDatasetDimensions, List<UploadedDataPoint>>()

            datasetDimensions.forEach { dataDimensions ->
                val companyId = dataDimensions.companyId
                val reportingPeriod = dataDimensions.reportingPeriod
                val relevantTypes = dataCompositionService.getRelevantDataPointTypes(dataDimensions.framework)
                val missingDataPointTypes = dataAvailabilityChecker.getMissingDataPointTypes(relevantTypes, reportingPeriod, companyId)
                val calculatedDataPoints =
                    calculateDataPoints(
                        dataPointTypes = missingDataPointTypes,
                        companyId = companyId,
                        reportingPeriod = reportingPeriod,
                        correlationId = correlationId,
                    )
                if (calculatedDataPoints.isNotEmpty()) {
                    calculatedData[dataDimensions] = calculatedDataPoints
                }
            }
            return calculatedData
        }
    }
