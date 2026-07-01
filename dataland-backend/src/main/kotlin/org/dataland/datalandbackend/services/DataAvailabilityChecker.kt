package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackendutils.interfaces.DataDimensions
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.interfaces.DatasetDimensions
import org.dataland.datalandbackendutils.model.BasicBaseDimensions
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.distinct
import kotlin.collections.plus

/**
 * Service to determine if data is available
 */
@Service("DataAvailabilityChecker")
class DataAvailabilityChecker
    @Autowired
    constructor(
        private val dataCompositionService: DataCompositionService,
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val dataPointCalculator: DataPointCalculator,
        private val dataPointMetaInformationManager: DataPointMetaInformationManager,
        private val specificationService: SpecificationService,
    ) {
        /**
         * Retrieves metadata of active datasets for the given data dimensions ignoring invalid dimensions.
         *
         * @param dataDimensions List of data dimensions to search for.
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        private fun getMetaDataOfActiveDatasets(dataDimensions: List<DataDimensions>): List<DataMetaInformationEntity> =
            dataMetaInformationManager.getActiveDataMetaInformationList(
                dataCompositionService.filterOutInvalidDatasetDimensions(
                    dataDimensions.map { BasicDataDimensions(it.companyId, it.dataType, it.reportingPeriod) },
                ),
            )

        /**
         * Retrieves metadata of active datasets for the given filter.
         *
         * An empty list in the filter means no restrictions.
         *
         * @param dataDimensionQuery dimensions filter specifying what to search for
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        private fun getMetaDataOfActiveDatasets(dataDimensionQuery: DataDimensionQuery): List<DataMetaInformationEntity> {
            val datasetDimensionQuery = dataCompositionService.filterOutInvalidDatasetEntries(dataDimensionQuery)
            if (datasetDimensionQuery.dataTypes.isEmpty() && !dataDimensionQuery.dataTypes.isEmpty()) {
                return emptyList()
            }
            return dataMetaInformationManager.getActiveDataMetaInformationList(datasetDimensionQuery)
        }

        /**
         * Retrieves metadata of active data points that match the provided data point dimensions. Invalid dimensions are ignored.
         *
         * @param dataDimensions List of data point dimensions to search for.
         * @return List of DataPointMetaInformationEntity objects that match the provided data point dimensions.
         */
        private fun getMetaDataOfActiveDataPoints(dataDimensions: List<DataPointDimensions>): List<DataPointMetaInformationEntity> =
            dataPointMetaInformationManager.getActiveDataPointMetaInformationList(
                dataCompositionService.filterOutInvalidDataPointDimensions(dataDimensions).distinct(),
            )

        /**
         * Retrieves metadata of active datasets for the given filter.
         *
         * An empty list in the filter means no restrictions.
         *
         * @param dataDimensionQuery dimensions filter specifying what to search for
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        private fun getMetaDataOfActiveDataPoints(dataDimensionQuery: DataDimensionQuery): List<DataPointMetaInformationEntity> {
            val dataPointDimensionQuery = dataCompositionService.filterOutInvalidDataPointEntries(dataDimensionQuery)
            if (dataPointDimensionQuery.dataTypes.isEmpty() && !dataDimensionQuery.dataTypes.isEmpty()) {
                return emptyList()
            }
            return dataPointMetaInformationManager.getActiveDataPointMetaInformationList(dataPointDimensionQuery)
        }

        /**
         * Returns the subset of the provided data dimensions for which active data exists (dataset or data point).
         *
         * Both the dataset store and the data-point store are queried. Dimensions with an unknown dataType are
         * silently dropped. If only ignored fields are available for a certain dataset dimension, it will not be part
         * of the result.
         *
         * @param dimensions List of data dimensions
         * @return The subset of the input dimensions for which active data exists
         */
        fun filterViewableDimensions(dimensions: List<BasicDataDimensions>): List<BasicDataDimensions> {
            val dataPointBasedDimensions =
                getMetaDataOfActiveDataPoints(dimensions.map { it.toBasicDataPointDimensions() }).map { it.toBasicDataDimensions() }
            val nonAssembledFrameworkBasedDimensions = getMetaDataOfActiveDatasets(dimensions).map { it.toBasicDataDimensions() }
            val assembledFrameworkBasedDimensions = getAllViewableDimensionsForAssembledFrameworks(dimensions)

            return (dataPointBasedDimensions + nonAssembledFrameworkBasedDimensions + assembledFrameworkBasedDimensions).distinct()
        }

        /**
         * Searches for available data dimensions matching the provided filter criteria.
         *
         * Both the dataset store and the data-point store are queried. An empty list for any filter dimension is
         * treated as a wildcard (all values for that dimension are included). If only ignored fields are available for
         * a certain dataset dimension, it will not be part of the result.
         *
         * @param dataDimensionQuery dimensions filter specifying what to search for
         * @return All active data dimensions matching the filter criteria
         */
        fun searchViewableDimensions(dataDimensionQuery: DataDimensionQuery): List<BasicDataDimensions> {
            val dataPointBasedDimensions = getMetaDataOfActiveDataPoints(dataDimensionQuery).map { it.toBasicDataDimensions() }
            val nonAssembledFrameworkBasedDimensions = getAllViewableDimensionsForNonAssembledFrameworks(dataDimensionQuery)
            val assembledFrameworkBasedDimensions = getAllViewableDimensionsForAssembledFrameworks(dataDimensionQuery)

            return (dataPointBasedDimensions + nonAssembledFrameworkBasedDimensions + assembledFrameworkBasedDimensions).distinct()
        }

        /**
         * Retrieve all active non assembled framework-based data dimensions using the given DataDimensionQuery.
         *
         * If no dataType is specified, all frameworks are taken into account.
         *
         * @param dataDimensionQuery the filter to use when searching for active data dimensions
         * @return a list of active framework data dimensions
         */
        private fun getAllViewableDimensionsForNonAssembledFrameworks(dataDimensionQuery: DataDimensionQuery): List<BasicDataDimensions> {
            val allNonAssembledFrameworks = specificationService.getNonAssembledFrameworks()
            val frameworks =
                if (dataDimensionQuery.dataTypes.isEmpty()) {
                    allNonAssembledFrameworks
                } else {
                    dataDimensionQuery.dataTypes.intersect(allNonAssembledFrameworks)
                }
            if (frameworks.isEmpty()) return emptyList()

            return getMetaDataOfActiveDatasets(
                DataDimensionQuery(
                    dataDimensionQuery.companyIds,
                    frameworks.toList(),
                    dataDimensionQuery.reportingPeriods,
                ),
            ).map { it.toBasicDataDimensions() }
        }

        private fun getAllViewableDimensionsForAssembledFrameworks(dimensions: List<BasicDataDimensions>): List<BasicDataDimensions> =
            dataCompositionService
                .filterOutInvalidDatasetDimensions(dimensions)
                .filter { specificationService.isAssembledFramework(it.framework) }
                .flatMap { dimension ->
                    dataCompositionService
                        .getRelevantDataPointTypes(dimension.framework)
                        .map { dataPointType ->
                            dimension.framework to
                                BasicDataPointDimensions(
                                    dimension.companyId,
                                    dataPointType,
                                    dimension.reportingPeriod,
                                )
                        }
                }.groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second },
                ).flatMap { (framework, dataPointDimensions) ->
                    getViewableDatasetDimensions(dataPointDimensions, framework)
                }

        /**
         * Retrieve all active assembled framework-based data dimensions using the given DataDimensionQuery.
         *
         * If no dataType is specified, all frameworks are taken into account.
         *
         * @param dataDimensionQuery the filter to use when searching for active data dimensions
         * @return a list of active framework data dimensions
         */
        private fun getAllViewableDimensionsForAssembledFrameworks(dataDimensionQuery: DataDimensionQuery): Set<BasicDataDimensions> {
            val allAssembledFrameworks = specificationService.getAssembledFrameworks()
            val frameworks =
                if (dataDimensionQuery.dataTypes.isEmpty()) {
                    allAssembledFrameworks
                } else {
                    dataDimensionQuery.dataTypes.intersect(allAssembledFrameworks)
                }

            return frameworks
                .flatMap { framework ->
                    val dataPointTypes = dataCompositionService.getRelevantDataPointTypes(framework)
                    val activeDataPointDimensions =
                        getMetaDataOfActiveDataPoints(
                            DataDimensionQuery(
                                companyIds = dataDimensionQuery.companyIds,
                                dataTypes = dataPointTypes.toList(),
                                reportingPeriods = dataDimensionQuery.reportingPeriods,
                            ),
                        ).map { it.toBasicDataPointDimensions() }.toSet()

                    val calculatableDataPointDimensions =
                        dataPointCalculator.getCalculatableDataPointDimensions(
                            dataPointTypes = dataPointTypes,
                            dataDimensionQuery = dataDimensionQuery,
                        )

                    getViewableDatasetDimensions(activeDataPointDimensions + calculatableDataPointDimensions, framework)
                }.toSet()
        }

        /**
         * Returns viewable dataset dimensions for a given collection of data point dimensions.
         *
         * The data point dimensions are grouped by (companyId, reportionPeriod) and for each group, the corresponding
         * dataset dimensions is returned if and only if the group contains more than the ignored fields.
         *
         * This function is only meaningful, if the data point types of the passed data point dimensions are part of the
         * given framework.
         *
         * @param dataPointDimensions the available data point dimensions that determine if a dataset dimension is available
         * @param framework the framework used to construct the dataset dimensions
         * @return a set of viewable dataset dimensions
         */
        private fun getViewableDatasetDimensions(
            dataPointDimensions: Collection<BasicDataPointDimensions>,
            framework: String,
        ): Set<BasicDataDimensions> =
            dataPointDimensions
                .groupBy { it.toBaseDimensions() }
                .mapNotNull { (baseDimensions, dataPointDimensions) ->
                    if (DataAvailabilityIgnoredFieldsUtils.containsNonIgnoredDataPoints(dataPointDimensions.map { it.dataPointType })) {
                        BasicDataDimensions(baseDimensions.companyId, framework, baseDimensions.reportingPeriod)
                    } else {
                        null
                    }
                }.toSet()

        /**
         * Retrieves all active data point metadata for each given set of dataset dimensions.
         *
         * This is the batched equivalent of checking one list of data point dimensions, and performs only one metadata
         * lookup across all requested data point dimensions.
         *
         * @param dataPointDimensionsByDatasetDimensions map from dataset dimensions to the data point dimensions to check
         * @return a map with the same dataset-dimension keys and the viewable metadata for each dimension
         */
        fun <T : DatasetDimensions> getViewableDataPointMetaData(
            dataPointDimensionsByDatasetDimensions: Map<T, Collection<BasicDataPointDimensions>>,
        ): Map<T, List<DataPointMetaInformationEntity>> {
            val allRelevantDimensions = dataPointDimensionsByDatasetDimensions.values.flatten()
            val metaDataByDimensions =
                getMetaDataOfActiveDataPoints(allRelevantDimensions)
                    .groupBy {
                        BasicDataPointDimensions(
                            companyId = it.companyId,
                            dataPointType = it.dataPointType,
                            reportingPeriod = it.reportingPeriod,
                        )
                    }

            return dataPointDimensionsByDatasetDimensions.mapValues { (_, dataPointDimensions) ->
                val metaData =
                    dataPointDimensions
                        .toSet()
                        .flatMap { metaDataByDimensions.getOrDefault(it, emptyList()) }
                if (DataAvailabilityIgnoredFieldsUtils.containsNonIgnoredDataPoints(metaData.map { it.dataPointType })) {
                    metaData
                } else {
                    emptyList()
                }
            }
        }

        /**
         * Returns most recent data point meta information entities.
         *
         * Retrieves the latest available data point meta information entities from a given collection,
         * ignoring data points that are part of the exclusion list. All meta information items in the
         * returned list belong to the same latest reporting period.
         *
         * @param entities the collection of data point meta information entities to consider
         * @return a list of data point meta information entities representing the latest available data points
         */
        private fun getMostRecentDataPointMetaInformationEntities(
            entities: Collection<DataPointMetaInformationEntity>,
        ): List<DataPointMetaInformationEntity> {
            val groupedByReportingPeriod =
                entities
                    .groupBy { it.reportingPeriod }
                    .filterValues { sameReportingPeriodEntities ->
                        DataAvailabilityIgnoredFieldsUtils.containsNonIgnoredDataPoints(
                            sameReportingPeriodEntities.map { it.dataPointType },
                        )
                    }
            val latestReportingPeriod = groupedByReportingPeriod.maxOfOrNull { it.key } ?: return emptyList()
            return groupedByReportingPeriod.getValue(latestReportingPeriod)
        }

        /**
         * Returns most recent available data point metadata per company.
         *
         * Retrieves the latest available data points for a collection of companies and set of data point types,
         * ignoring data points that are part of the exclusion list. For each company, all meta information items
         * in the returned list belong to the same latest reporting period.
         *
         * If no data is available for a company, it is omitted from the result.
         *
         * @param companyIds the IDs of the companies
         * @param dataPointTypes the set of data point types to consider
         * @return a map of company and reporting-period dimensions, each associated with the latest available data point metadata
         */
        fun getLatestAvailableDataPointIds(
            companyIds: Collection<String>,
            dataPointTypes: Set<String>,
        ): Map<BasicBaseDimensions, List<DataPointMetaInformationEntity>> =
            getMetaDataOfActiveDataPoints(DataDimensionQuery(companyIds.toList(), dataPointTypes.toList()))
                .groupBy { it.companyId }
                .entries
                .asSequence()
                .mapNotNull {
                    val dataPointMetaInfos = getMostRecentDataPointMetaInformationEntities(it.value)
                    if (dataPointMetaInfos.isEmpty()) {
                        null
                    } else {
                        BasicBaseDimensions(it.key, dataPointMetaInfos.first().reportingPeriod) to dataPointMetaInfos
                    }
                }.toMap()
    }
