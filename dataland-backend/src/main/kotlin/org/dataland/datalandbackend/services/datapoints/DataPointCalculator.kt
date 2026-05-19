package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
        private val specificationService: SpecificationService,
    ) {
        private fun removeDataPointsWithoutValue(dataPoints: Collection<UploadedDataPoint>): Collection<UploadedDataPoint> {
            val result = mutableListOf<UploadedDataPoint>()
            dataPoints.forEach { uploadedDataPoint ->
                try {
                    // Cast to Any? since the value can be of any type and we only want to check for nullability here, the actual type is not relevant
                    val castedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<Any?>>(uploadedDataPoint.dataPoint)
                    if (castedDataPoint.value != null) {
                        result.add(uploadedDataPoint)
                    }
                } catch (ignore: Exception) {
                    // Skipping data point as it cannot be cast into an extended data point
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

        /**
         * Attempts to calculate the values for the [dataPointTypes] and the fixed [reportingPeriod] and [companyId].
         * If multiple calculation rules are possible the first one with all sources available will be used.
         * @return A list of all calculated data points (is empty if no calculation was possible)
         */
        private fun calculateDataPoints(
            dataPointTypes: Collection<DataPointType>,
            companyId: String,
            reportingPeriod: String,
            correlationId: String,
        ): List<UploadedDataPoint> {
            val potentialCalculations = dataCompositionService.getAvailableCalculationRules(dataPointTypes)
            val allSourceTypes = potentialCalculations.values.flatten().flatMap { it.inputs }
            val allSourceData = getAvailableSourceData(allSourceTypes, reportingPeriod, companyId, correlationId)
            val allSourceDataByType = allSourceData.associateBy { it.dataPointType }
            val allAvailableSourceTypes = allSourceDataByType.keys
            val calculatedDataPoints = mutableListOf<UploadedDataPoint>()
            potentialCalculations.forEach potentialCalculationLoop@{ (dataPointType, calculationRules) ->
                calculationRules.forEach calculationRulesLoop@{ calculationRule ->
                    if (allAvailableSourceTypes.containsAll(calculationRule.inputs)) {
                        val targetDimensions =
                            BasicDataPointDimensions(
                                companyId = companyId,
                                dataPointType = dataPointType,
                                reportingPeriod = reportingPeriod,
                            )
                        val orderedInputs =
                            calculationRule.inputs.map { sourceType ->
                                allSourceDataByType.getValue(sourceType)
                            }
                        val calculatedDataPoint =
                            try {
                                calculateSingleDataPoint(
                                    inputs = orderedInputs,
                                    method = calculationRule.calculationMethod,
                                    dataPointDimensions = targetDimensions,
                                )
                            } catch (exception: IllegalArgumentException) {
                                // Skip this rule and continue with the next
                                return@calculationRulesLoop
                            }
                        // Rule was successfully applied, add the calculated data point to the result and do not attempt further rules for this type
                        calculatedDataPoints.add(calculatedDataPoint)
                        return@potentialCalculationLoop
                    }
                }
            }
            return calculatedDataPoints
        }

        private fun calculateSingleDataPoint(
            inputs: Collection<UploadedDataPoint>,
            method: String,
            dataPointDimensions: BasicDataPointDimensions,
        ): UploadedDataPoint {
            val specs = specificationService.getDataPointSpecifications(inputs.map { it.dataPointType })
            return applyTransformation(
                inputs = inputs,
                targetType = dataPointDimensions.dataPointType,
                method = method,
                specs = specs,
            )
        }

        /**
         * Derives the missing data points for each of the given dataset dimensions and returns them grouped per dimension.
         * For every dimension only the data point types that are not already available are calculated;
         * dimensions for which nothing could be derived are omitted from the result.
         * @param datasetDimensions the dataset dimensions for which calculated data should be produced
         * @param correlationId correlation id propagated to downstream calls for tracing
         * @return a map from each dataset dimension to the list of newly calculated data points
         */
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
