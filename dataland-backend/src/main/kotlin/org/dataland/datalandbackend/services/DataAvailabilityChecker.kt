package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackendutils.model.BasicBaseDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service to determine if data is available
 */
@Service("DataAvailabilityChecker")
class DataAvailabilityChecker
    @Autowired
    constructor(
        @PersistenceContext private val entityManager: EntityManager,
        private val dataCompositionService: DataCompositionService,
        private val dataPointMetaInformationRepository: DataPointMetaInformationRepository,
    ) {
        /**
         * Retrieves metadata of active datasets for the given data dimensions ignoring invalid dimensions.
         * @param dataDimensions List of data dimensions to search for.
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        fun getMetaDataOfActiveDatasets(dataDimensions: List<BasicDatasetDimensions>): List<DataMetaInformation> {
            val dimensionsToProcess = dataCompositionService.filterOutInvalidDatasetDimensions(dataDimensions)
            val formattedTuples =
                dimensionsToProcess.joinToString(", ") {
                    "('${it.companyId}', '${it.framework}', '${it.reportingPeriod}')"
                }

            val queryToExecute =
                """SELECT * FROM data_meta_information
                WHERE (company_id, data_type, reporting_period) IN ($formattedTuples)
                AND currently_active = true"""

            return if (dimensionsToProcess.isNotEmpty()) {
                val query = entityManager.createNativeQuery(queryToExecute, DataMetaInformationEntity::class.java)
                return query.resultList
                    .filterIsInstance<DataMetaInformationEntity>()
                    .map { it.toApiModel() }
            } else {
                emptyList()
            }
        }

        /**
         * Retrieves metadata of active data points that match the provided data point dimensions. Invalid dimensions are ignored.
         * @param dataDimensions List of data point dimensions to search for.
         * @return List of DataPointMetaInformationEntity objects that match the provided data point dimensions.
         */
        fun getMetaDataOfActiveDataPoints(dataDimensions: List<BasicDataPointDimensions>): List<DataPointMetaInformationEntity> {
            val dimensionsToProcess = dataCompositionService.filterOutInvalidDataPointDimensions(dataDimensions)
            val formattedTuples =
                dimensionsToProcess.joinToString(", ") {
                    "('${it.companyId}', '${it.dataPointType}', '${it.reportingPeriod}')"
                }

            val queryToExecute =
                """SELECT * FROM data_point_meta_information
                WHERE (company_id, data_point_type, reporting_period) IN ($formattedTuples)
                AND currently_active = true"""

            return if (dimensionsToProcess.isNotEmpty()) {
                val query = entityManager.createNativeQuery(queryToExecute, DataPointMetaInformationEntity::class.java)
                return query.resultList.filterIsInstance<DataPointMetaInformationEntity>()
            } else {
                emptyList()
            }
        }

        /**
         * Retrieves all active data point IDs that correspond to the data point dimensions provided.
         * Only returns IDs if at least one data point is not an ignorable fields.
         * @param dataDimensions the list of data point dimensions to get the data point IDs for
         * @return a list of data point IDs corresponding to the viewable data points of the input
         */
        fun getViewableDataPointIds(dataDimensions: List<BasicDataPointDimensions>): List<String> {
            val metaData = getMetaDataOfActiveDataPoints(dataDimensions)
            return if (DataAvailabilityIgnoredFieldsUtils.containsNonIgnoredDataPoints(metaData.map { it.dataPointType })) {
                metaData.map { it.dataPointId }
            } else {
                emptyList()
            }
        }

        /** Returns most recent data point meta information entities.
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
         * Returns most recent available data point IDs per company.
         *
         * Retrieves the latest available data points for a collection of companies and set of data point types,
         * ignoring data points that are part of the exclusion list. For each company, all meta information items
         * in the returned list belong to the same latest reporting period.
         *
         * If no data is available for a company, it is omitted from the result.
         *
         * @param companyIds the IDs of the companies
         * @param dataPointTypes the set of data point types to consider
         * @return a map of company IDs, each associated with a list of IDs representing the latest available data points
         */
        fun getLatestAvailableDataPointIds(
            companyIds: Collection<String>,
            dataPointTypes: Set<String>,
        ): Map<BasicBaseDimensions, List<DataPointMetaInformationEntity>> =
            dataPointMetaInformationRepository
                .findByCompanyIdInAndDataPointTypeInAndCurrentlyActiveTrue(
                    companyIds,
                    dataPointTypes,
                ).groupBy { it.companyId }
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
