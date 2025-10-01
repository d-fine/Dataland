package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataSetDimensions
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
    ) {
        /**
         * Retrieves metadata of active datasets for the given data dimensions ignoring invalid dimensions.
         * @param dataDimensions List of data dimensions to search for.
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        fun getMetaDataOfActiveDatasets(dataDimensions: List<BasicDataSetDimensions>): List<DataMetaInformation> {
            val dimensionsToProcess = dataCompositionService.filterOutInvalidDataSetDimensions(dataDimensions)
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
         * Retrieves metadata of data points that match the provided data point dimensions irrespective of the active status.
         * Invalid dimensions are ignored.
         * @param dataDimensions List of data point dimensions to search for.
         * @return List of DataPointMetaInformationEntity objects that match the provided data point dimensions.
         */
        fun getMetaDataOfDataPoints(dataDimensions: List<BasicDataPointDimensions>): List<DataPointMetaInformationEntity> {
            val dimensionsToProcess = dataCompositionService.filterOutInvalidDataPointDimensions(dataDimensions)
            val formattedTuples =
                dimensionsToProcess.joinToString(", ") {
                    "('${it.companyId}', '${it.dataPointType}', '${it.reportingPeriod}')"
                }

            val queryToExecute =
                """SELECT * FROM data_point_meta_information
                WHERE (company_id, data_point_type, reporting_period) IN ($formattedTuples)"""

            return if (dimensionsToProcess.isNotEmpty()) {
                val query = entityManager.createNativeQuery(queryToExecute, DataPointMetaInformationEntity::class.java)
                return query.resultList.filterIsInstance<DataPointMetaInformationEntity>()
            } else {
                emptyList()
            }
        }

        /**
         * Retrieves all data point IDs that correspond to the data point dimensions provided. Filters out results that are not viewable by
         * the current user. Only returns IDs if at least one data point is viewable that is not an ignorable fields.
         * @param dataDimensions the list of data point dimensions to get the data point IDs for
         * @return a list of data point IDs corresponding to the viewable data points of the input
         */
        fun getViewableDataPointIds(dataDimensions: List<BasicDataPointDimensions>): List<String> {
            val metadata = getMetaDataOfDataPoints(dataDimensions)
            val viewableMetaData = metadata.filter { it.isDatasetViewableByUser(viewingUser = DatalandAuthentication.fromContextOrNull()) }
            return if (DataAvailabilityIgnoredFieldsUtils.containsNonIgnoredDataPoints(viewableMetaData.map { it.dataPointType })) {
                viewableMetaData.map { it.dataPointId }
            } else {
                emptyList()
            }
        }
    }
