package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackendutils.model.BasicBaseDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Filters out uploaded data points whose serialized extended data point has a null value.
         * Data points that cannot be parsed as extended data points are skipped.
         * @param dataPoints uploaded data points to inspect
         * @return all parseable uploaded data points with non-null values
         */
        private fun removeDataPointsWithoutValue(dataPoints: Collection<UploadedDataPoint>): Collection<UploadedDataPoint> {
            val result = mutableListOf<UploadedDataPoint>()
            dataPoints.forEach { uploadedDataPoint ->
                try {
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

        /**
         * Retrieves all available source data points for the requested data point types and groups them by dataset dimension.
         * Source data points without values are omitted before grouping.
         * @param dataPointTypesByDatasetDimension source data point types required for each dataset dimension
         * @param correlationId correlation id propagated to internal storage reads
         * @return available source data points grouped by their requested dataset dimensions
         */
        private fun getAvailableSourceDataByDatasetDimension(
            dataPointTypesByDatasetDimension: Map<BasicDatasetDimensions, Collection<DataPointType>>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, Collection<UploadedDataPoint>> {
            val allDimensions =
                dataPointTypesByDatasetDimension
                    .flatMap { (datasetDimension, dataPointTypes) ->
                        datasetDimension.toBasicDataPointDimensions(dataPointTypes)
                    }
            val allAvailableIds = dataAvailabilityChecker.getViewableDataPointIds(allDimensions)
            val allStoredDataPoints =
                internalStorageAdapter
                    .retrieveDataPointsFromInternalStorage(dataPointIds = allAvailableIds, correlationId = correlationId)
            val allStoredDataPointsWithValues = removeDataPointsWithoutValue(allStoredDataPoints.values)
            val datasetDimensions = dataPointTypesByDatasetDimension.keys
            val sourceDataByBaseDimensions =
                allStoredDataPointsWithValues.groupBy {
                    BasicBaseDimensions(
                        companyId = it.companyId,
                        reportingPeriod = it.reportingPeriod,
                    )
                }
            return datasetDimensions.associateWith { datasetDimension ->
                sourceDataByBaseDimensions.getOrDefault(datasetDimension.toBaseDimensions(), emptyList())
            }
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
            return potentialCalculations.mapNotNull { (dataPointType, calculationRules) ->
                calculateFirstAvailableDataPoint(
                    dataPointType = dataPointType,
                    calculationRules = calculationRules,
                    allSourceDataByType = allSourceDataByType,
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                )
            }
        }

        /**
         * Applies the first available calculation rule for one target data point type.
         * Rules are evaluated in their given order; rules with missing inputs or failed calculations are skipped.
         * @param dataPointType target data point type to calculate
         * @param calculationRules candidate rules for the target data point type
         * @param allSourceDataByType available source data points by data point type
         * @param companyId company id of the target data point
         * @param reportingPeriod reporting period of the target data point
         * @return the first successfully calculated data point, or null if no rule can be applied
         */
        private fun calculateFirstAvailableDataPoint(
            dataPointType: DataPointType,
            calculationRules: Collection<CalculationRule>,
            allSourceDataByType: Map<DataPointType, UploadedDataPoint>,
            companyId: String,
            reportingPeriod: String,
        ): UploadedDataPoint? {
            val targetDimensions =
                BasicDataPointDimensions(
                    companyId = companyId,
                    dataPointType = dataPointType,
                    reportingPeriod = reportingPeriod,
                )
            return calculationRules.firstNotNullOfOrNull { calculationRule ->
                if (!allSourceDataByType.keys.containsAll(calculationRule.inputs)) {
                    return@firstNotNullOfOrNull null
                }
                tryCalculateSingleDataPoint(
                    calculationRule = calculationRule,
                    dataPointType = dataPointType,
                    allSourceDataByType = allSourceDataByType,
                    targetDimensions = targetDimensions,
                )
            }
        }

        /**
         * Calculates one data point from a rule and converts expected calculation failures into null.
         * @param calculationRule calculation rule to apply
         * @param dataPointType target data point type used for logging
         * @param allSourceDataByType available source data points by data point type
         * @param targetDimensions dimensions of the data point to calculate
         * @return calculated data point, or null if the rule cannot be applied
         */
        private fun tryCalculateSingleDataPoint(
            calculationRule: CalculationRule,
            dataPointType: DataPointType,
            allSourceDataByType: Map<DataPointType, UploadedDataPoint>,
            targetDimensions: BasicDataPointDimensions,
        ): UploadedDataPoint? {
            val orderedInputs =
                calculationRule.inputs.map { sourceType ->
                    allSourceDataByType.getValue(sourceType)
                }
            return try {
                calculateSingleDataPoint(
                    inputs = orderedInputs,
                    method = calculationRule.calculationMethod,
                    dataPointDimensions = targetDimensions,
                )
            } catch (exception: IllegalArgumentException) {
                logger.error(
                    "Skipping calculation rule for data point type $dataPointType, companyId ${targetDimensions.companyId}, " +
                        "reportingPeriod ${targetDimensions.reportingPeriod}, method ${calculationRule.calculationMethod}, " +
                        "inputs ${calculationRule.inputs}.",
                    exception,
                )
                null
            }
        }

        /**
         * Applies the named transformation method to ordered source data points.
         * Required source and target data point specifications are loaded before transformation.
         * @param inputs ordered source data points for the transformation
         * @param method calculation method name
         * @param dataPointDimensions target data point dimensions
         * @return the transformed uploaded data point
         */
        private fun calculateSingleDataPoint(
            inputs: Collection<UploadedDataPoint>,
            method: String,
            dataPointDimensions: BasicDataPointDimensions,
        ): UploadedDataPoint {
            val dataPointTypes = inputs.map { it.dataPointType } + dataPointDimensions.dataPointType
            val specs = specificationService.getDataPointSpecifications(dataPointTypes.distinct())
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
         * @param deliverableDataPointDimensions already deliverable data point dimensions by dataset dimension
         * @param correlationId correlation id propagated to downstream calls for tracing
         * @return a map from each dataset dimension to the list of newly calculated data points
         */
        fun getCalculatedData(
            datasetDimensions: Collection<BasicDatasetDimensions>,
            deliverableDataPointDimensions: Map<BasicDatasetDimensions, Collection<BasicDataPointDimensions>>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, List<UploadedDataPoint>> {
            val missingDataPointTypesByDatasetDimension =
                datasetDimensions.associateWith { datasetDimensions ->
                    val relevantTypes = dataCompositionService.getRelevantDataPointTypes(datasetDimensions.framework)
                    val availableTypes =
                        deliverableDataPointDimensions
                            .getOrDefault(datasetDimensions, emptyList())
                            .map { it.dataPointType }
                            .toSet()
                    relevantTypes - availableTypes
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

            val calculatedData =
                potentialCalculationsByDatasetDimension
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
            return calculatedData
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
        ): Set<BasicDataPointDimensions> =
            getActiveSourceDataPointDimensions(
                dataPointTypes = dataPointTypes,
                dataDimensionFilter = DataDimensionFilter(companyIds = listOf(companyId)),
            )

        /**
         * Finds active source data point dimensions that can be used to calculate any of the given target data point types.
         *
         * A source data point dimension is returned only if all inputs of at least one calculation rule are active for the
         * same company and reporting period. Company and reporting-period filters are applied to the source data points.
         *
         * @param dataPointTypes target data point types whose calculation rules should be checked
         * @param dataDimensionFilter filter whose company and reporting-period constraints should be applied
         * @return active source dimensions grouped by calculatable company/reporting-period pairs
         */
        fun getActiveSourceDataPointDimensions(
            dataPointTypes: Collection<DataPointType>,
            dataDimensionFilter: DataDimensionFilter,
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
                    .getActiveDataPointMetaInformationList(
                        DataDimensionFilter(
                            companyIds = dataDimensionFilter.companyIds,
                            dataTypes = sourceDataPointTypes.distinct(),
                            reportingPeriods = dataDimensionFilter.reportingPeriods,
                        ),
                    ).map {
                        it.toBasicDataPointDimensions()
                    }.groupBy {
                        BasicBaseDimensions(
                            companyId = it.companyId,
                            reportingPeriod = it.reportingPeriod,
                        )
                    }

            return specs.values
                .flatMap { specification ->
                    specification.calculationRules.orEmpty()
                }.flatMap { calculationRule ->
                    getActiveSourceDataPointDimensionsForRule(
                        calculationRule = calculationRule,
                        activeSourceDataPointDimensionsByPeriod = activeSourceDataPointDimensionsByPeriod,
                    )
                }.toSet()
        }

        /**
         * Returns source dimensions for all company/reporting-period groups where the given rule is fully satisfiable.
         * @param calculationRule calculation rule whose inputs must be active
         * @param activeSourceDataPointDimensionsByPeriod active source dimensions grouped by base dimensions
         * @return active source dimensions that satisfy the rule
         */
        private fun getActiveSourceDataPointDimensionsForRule(
            calculationRule: CalculationRule,
            activeSourceDataPointDimensionsByPeriod: Map<BasicBaseDimensions, List<BasicDataPointDimensions>>,
        ): List<BasicDataPointDimensions> =
            activeSourceDataPointDimensionsByPeriod
                .filter { (_, dimensions) ->
                    dimensions.map { it.dataPointType }.toSet().containsAll(calculationRule.inputs)
                }.flatMap { (baseDimensions, _) ->
                    calculationRule.inputs.map { dataPointType ->
                        BasicDataPointDimensions(
                            companyId = baseDimensions.companyId,
                            dataPointType = dataPointType,
                            reportingPeriod = baseDimensions.reportingPeriod,
                        )
                    }
                }
    }
