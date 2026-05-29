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
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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
        private val metaDataManager: DataPointMetaInformationManager,
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
                } catch (_: Exception) {
                    // Skipping data point as it cannot be cast into an extended data point
                }
            }
            return result
        }

        private fun getAvailableSourceDataByDatasetDimension(
            dataPointTypesByDatasetDimension: Map<BasicDatasetDimensions, Collection<DataPointType>>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, Collection<UploadedDataPoint>> {
            val allDimensions =
                dataPointTypesByDatasetDimension
                    .map { (datasetDimension, dataPointTypes) ->
                        dataPointTypes.map { dataPointType ->
                            BasicDataPointDimensions(
                                companyId = datasetDimension.companyId,
                                reportingPeriod = datasetDimension.reportingPeriod,
                                dataPointType = dataPointType,
                            )
                        }
                    }.flatten()
            val allAvailableIds = dataAvailabilityChecker.getViewableDataPointIds(allDimensions)
            val allStoredDataPoints =
                internalStorageAdapter
                    .retrieveDataPointsFromInternalStorage(dataPointIds = allAvailableIds, correlationId = correlationId)
            val allStoredDataPointsWithValues = removeDataPointsWithoutValue(allStoredDataPoints.values)
            val datasetDimensions = dataPointTypesByDatasetDimension.keys
            val associateBy =
                datasetDimensions.associateWith { datasetDimension ->
                    allStoredDataPointsWithValues.filter { dataPoint ->
                        dataPoint.companyId == datasetDimension.companyId && dataPoint.reportingPeriod == datasetDimension.reportingPeriod
                    }
                }
            return associateBy
        }

        /**
         * Attempts to calculate data points for the fixed [reportingPeriod] and [companyId].
         * [potentialCalculations] contains the target data point types and their candidate calculation rules.
         * [allSourceData] must contain only source data for the same company and reporting period.
         * If multiple calculation rules are possible, the first one with all required sources available is used.
         * @return A list of all calculated data points (is empty if no calculation was possible)
         */
        private fun calculateDataPoints(
            potentialCalculations: Map<DataPointType, Collection<CalculationRule>>,
            allSourceData: Collection<UploadedDataPoint>,
            companyId: String,
            reportingPeriod: String,
        ): List<UploadedDataPoint> {
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
                            } catch (_: IllegalArgumentException) {
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
            val missingDataPointTypesByDatasetDimension =
                datasetDimensions.associateWith { datasetDimensions ->
                    val relevantTypes = dataCompositionService.getRelevantDataPointTypes(datasetDimensions.framework)
                    dataAvailabilityChecker
                        .getMissingDataPointTypes(
                            relevantTypes, datasetDimensions.reportingPeriod,
                            datasetDimensions.companyId,
                        )
                }

            val potentialCalculationsByDatasetDimension =
                missingDataPointTypesByDatasetDimension.mapValues { (_, missingDataPointTypes) ->
                    dataCompositionService.getAvailableCalculationRules(missingDataPointTypes)
                }
            val sourceTypesByDatasetDimensions =
                potentialCalculationsByDatasetDimension.mapValues { (_, calculationRules) ->
                    calculationRules.values
                        .flatten()
                        .flatMap { it.inputs }
                        .distinct()
                }

            val sourceDataByDatasetDimensions =
                getAvailableSourceDataByDatasetDimension(
                    sourceTypesByDatasetDimensions,
                    correlationId = correlationId,
                )

            return potentialCalculationsByDatasetDimension
                .mapNotNull { (datasetDimensions, potentialCalculations) ->
                    val calculatedDataPoints =
                        calculateDataPoints(
                            companyId = datasetDimensions.companyId,
                            reportingPeriod = datasetDimensions.reportingPeriod,
                            potentialCalculations = potentialCalculations,
                            allSourceData = sourceDataByDatasetDimensions.getValue(datasetDimensions),
                        )
                    calculatedDataPoints.takeIf { it.isNotEmpty() }?.let { datasetDimensions to it }
                }.toMap()
        }

        /**
         * Finds active source data point dimensions that can be used to calculate any of the given target data point types.
         *
         * A source data point dimension is returned only if all inputs of at least one calculation rule are active for the
         * same company and reporting period.
         *
         * @param dataPointTypes target data point types whose calculation rules should be checked
         * @param companyId company for which active source data points should be considered
         * @return active source dimensions grouped by calculatable reporting periods
         */
        fun getActiveSourceDataPointDimensions(
            dataPointTypes: Collection<DataPointType>,
            companyId: String,
        ): Set<BasicDataPointDimensions> {
            val specs = specificationService.getDataPointSpecifications(dataPointTypes.toList())

            val sourceDataPointTypes =
                specs.flatMap { (_, specification) ->
                    specification.calculationRules?.flatMap { calculationRule ->
                        calculationRule.inputs
                    } ?: emptyList()
                }

            val activeSourceDataPointDimensionsByPeriod =
                metaDataManager
                    .getActiveDataPointMetaInformation(dataPointTypes = sourceDataPointTypes.toSet(), companyId = companyId)
                    .map {
                        BasicDataPointDimensions(
                            companyId = it.companyId,
                            dataPointType = it.dataPointType,
                            reportingPeriod = it.reportingPeriod,
                        )
                    }.groupBy { it.reportingPeriod }

            val validAndActiveSourceDataPointDimensions = mutableSetOf<BasicDataPointDimensions>()
            specs.forEach { (_, specification) ->
                specification.calculationRules?.forEach { calculationRule ->
                    activeSourceDataPointDimensionsByPeriod.forEach { (reportingPeriod, dimensions) ->
                        if (dimensions.map { it.dataPointType }.toSet().containsAll(calculationRule.inputs)) {
                            calculationRule.inputs.forEach {
                                validAndActiveSourceDataPointDimensions.add(
                                    BasicDataPointDimensions(
                                        companyId = companyId,
                                        dataPointType = it,
                                        reportingPeriod = reportingPeriod,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            return validAndActiveSourceDataPointDimensions
        }
    }
