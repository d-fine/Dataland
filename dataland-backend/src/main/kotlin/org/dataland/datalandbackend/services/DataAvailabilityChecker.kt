package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionFilter
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
         * @param dataDimensionFilter dimensions filter specifying what to search for
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        private fun getMetaDataOfActiveDatasets(dataDimensionFilter: DataDimensionFilter): List<DataMetaInformationEntity> {
            val datasetDimensionFilter = dataCompositionService.filterOutInvalidDatasetEntries(dataDimensionFilter)
            if (datasetDimensionFilter.dataTypes.isNullOrEmpty() && !dataDimensionFilter.dataTypes.isNullOrEmpty()) {
                return emptyList()
            }
            return dataMetaInformationManager.getActiveDataMetaInformationList(datasetDimensionFilter)
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
         * @param dataDimensionFilter dimensions filter specifying what to search for
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        private fun getMetaDataOfActiveDataPoints(dataDimensionFilter: DataDimensionFilter): List<DataPointMetaInformationEntity> {
            val dataPointDimensionFilter = dataCompositionService.filterOutInvalidDataPointEntries(dataDimensionFilter)
            if (dataPointDimensionFilter.dataTypes.isNullOrEmpty() && !dataDimensionFilter.dataTypes.isNullOrEmpty()) {
                return emptyList()
            }
            return dataPointMetaInformationManager.getActiveDataPointMetaInformationList(dataPointDimensionFilter)
        }

        /**
         * Checks which of the provided data dimensions have active data (dataset or data point).
         *
         * Both the dataset store and the data-point store are queried. Dimensions with an unknown dataType are
         * silently dropped. If only ignored fields are available for a certain dimension, it will not be part of the
         * result.
         *
         * @param dimensions List of data dimensions
         * @return The subset of the input dimensions for which active data exists
         */
        fun getAvailableDimensions(dimensions: List<BasicDataDimensions>): List<BasicDataDimensions> {
            if (dimensions.isEmpty()) return emptyList()

            val activeDatasetDimensions =
                getMetaDataOfActiveDatasets(dimensions)
                    .map { BasicDataDimensions(it.company.companyId, it.dataType, it.reportingPeriod) }

            val activeDataPointDimensions =
                getMetaDataOfActiveDataPoints(dimensions.map { it.toBasicDataPointDimensions() })
                    .map { BasicDataDimensions(it.companyId, it.dataPointType, it.reportingPeriod) }

            return (activeDatasetDimensions + activeDataPointDimensions).distinct()
        }

        /**
         * Searches for available data dimensions matching the provided filter criteria.
         *
         * Both the dataset store and the data-point store are queried. An empty list for any filter dimension is
         * treated as a wildcard (all values for that dimension are included). If only ignored fields are available for
         * a certain dimension, it will not be part of the result.
         *
         * @param dataDimensionFilter dimensions filter specifying what to search for
         * @return All active data dimensions matching the filter criteria
         */
        fun getViewableDimensions(dataDimensionFilter: DataDimensionFilter): List<BasicDataDimensions> {
            val dataPointBasedDimensions = getMetaDataOfActiveDataPoints(dataDimensionFilter).map { it.toBasicDataDimensions() }
            val nonAssembledFrameworkBasedDimensions = getAllViewableDimensionsForNonAssembledFrameworks(dataDimensionFilter)
            val assembledFrameworkBasedDimensions = getAllViewableDimensionsForAssembledFrameworks(dataDimensionFilter)

            return (dataPointBasedDimensions + nonAssembledFrameworkBasedDimensions + assembledFrameworkBasedDimensions).distinct()
        }

        /**
         * Retrieve all active non assembled framework-based data dimensions using the given DataDimensionFilter.
         *
         * If no dataType is specified, all frameworks are taken into account.
         *
         * @param dataDimensionFilter the filter to use when searching for active data dimensions
         * @return a list of active framework data dimensions
         */
        private fun getAllViewableDimensionsForNonAssembledFrameworks(dataDimensionFilter: DataDimensionFilter): List<BasicDataDimensions> {
            val allNonAssembledFrameworks = specificationService.getNonAssembledFrameworks()
            val frameworks =
                if (dataDimensionFilter.dataTypes.isNullOrEmpty()) {
                    allNonAssembledFrameworks
                } else {
                    dataDimensionFilter.dataTypes.intersect(allNonAssembledFrameworks)
                }
            if (frameworks.isEmpty()) return emptyList()

            return getMetaDataOfActiveDatasets(
                DataDimensionFilter(
                    dataDimensionFilter.companyIds,
                    frameworks.toList(),
                    dataDimensionFilter.reportingPeriods,
                ),
            ).map { it.toBasicDataDimensions() }
        }

        /**
         * Retrieve all active assembled framework-based data dimensions using the given DataDimensionFilter.
         *
         * If no dataType is specified, all frameworks are taken into account.
         *
         * @param dataDimensionFilter the filter to use when searching for active data dimensions
         * @return a list of active framework data dimensions
         */
        private fun getAllViewableDimensionsForAssembledFrameworks(dataDimensionFilter: DataDimensionFilter): Set<BasicDataDimensions> {
            val allAssembledFrameworks = specificationService.getAssembledFrameworks()
            val frameworks =
                if (dataDimensionFilter.dataTypes.isNullOrEmpty()) {
                    allAssembledFrameworks
                } else {
                    dataDimensionFilter.dataTypes.intersect(allAssembledFrameworks)
                }

            return frameworks
                .flatMap { framework ->
                    val dataPointTypes = dataCompositionService.getRelevantDataPointTypes(framework)
                    val activeDataPointDimensions =
                        getMetaDataOfActiveDataPoints(
                            DataDimensionFilter(
                                companyIds = dataDimensionFilter.companyIds,
                                dataTypes = dataPointTypes.toList(),
                                reportingPeriods = dataDimensionFilter.reportingPeriods,
                            ),
                        ).map { it.toBasicDataPointDimensions() }.toSet()

                    val calculatableDataPointDimensions =
                        dataPointCalculator.getCalculatableDataPointDimensions(
                            dataPointTypes = dataPointTypes,
                            dataDimensionFilter = dataDimensionFilter,
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
            getMetaDataOfActiveDataPoints(DataDimensionFilter(companyIds.toList(), dataPointTypes.toList()))
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
