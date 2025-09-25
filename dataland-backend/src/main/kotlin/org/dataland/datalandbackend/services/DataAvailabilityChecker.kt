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
        private val ignoredFields = DataAvailabilityIgnoredFieldsUtils.getIgnoredFields()

        /**
         * Retrieves metadata of datasets that are currently active for the given data dimensions.
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
         *
         * @param dataDimensions the list of data point dimensions to get the data point IDs for
         * @return a list of data point IDs corresponding to the viewable data points of the input
         */
        fun getViewableDataPointIds(dataDimensions: List<BasicDataPointDimensions>): List<String> {
            val metadata = getMetaDataOfDataPoints(dataDimensions)
            val viewableMetaData = metadata.filter { it.isDatasetViewableByUser(viewingUser = DatalandAuthentication.fromContextOrNull()) }
            val viewAbleMetaDataWithoutIgnoredFields = viewableMetaData.filter { ignoredFields.contains(it.dataPointType) }
            return if (viewAbleMetaDataWithoutIgnoredFields.isNotEmpty()) {
                viewableMetaData.map { it.dataPointId }
            } else {
                emptyList()
            }
        }
    }

/*class DataAvailabilityChecker
@Autowired
constructor(
    private val datasetMetaInformationRepository: DataMetaInformationRepository,
    private val dataPointMetaInformationRepository: DataPointMetaInformationRepository,
    private val datasetDatapointRepository: DatasetDatapointRepository,
    private val dataCompositionService: DataCompositionService,
    private val dataPointUtils: DataPointUtils,
) {
    private val logger = LoggerFactory.getLogger(javaClass)





    /**
 * Function to check if a dataset could be delivered to the user based on the dataset ID
 * @param datasetId the ID of the requested dataset
 * @return true if dataset can be delivered false otherwise
 */
    fun isDatasetDeliverable(datasetId: String): Boolean {
        val datasetMetainformationEntity = datasetMetaInformationRepository.findById(datasetId).getOrNull()
        return datasetMetainformationEntity?.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull()) ?: false
    }

    fun isAssembledDatasetDeliverable(datasetId: String): Boolean {
        val consistingDataPoints = datasetDatapointRepository.findById(datasetId).getOrNull()
        return consistingDataPoints?.dataPoints?.keys?.all { isDataPointDeliverable(it) } ?: false
    }

    fun isDataPointDeliverable(dataPointId: String): Boolean {
        val dataPointMetainformationEntity = dataPointMetaInformationRepository.findById(dataPointId).getOrNull()
        return dataPointMetainformationEntity?.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull()) ?: false
    }

    val ignoredDataPointTypes = arrayOf("Test1", "Test2")

    fun isDataDimensionActive(dataDimension: BasicDataDimensions): Boolean {
        // Option A: data represents a data point
        val activeDataPoint = dataPointMetaInformationRepository.getActiveDataPointId(dataDimension.toBasicDataPointDimensions())
        // Option B: data represents a dataset that was directly uploaded
        val activeDataset =
            datasetMetaInformationRepository
                .findActiveDatasetByReportingPeriodAndCompanyIdAndDataType(
                    dataDimension.reportingPeriod, dataDimension.companyId, dataDimension.dataType,
                )
        // Option C: data represents a framework that was not directly uploaded but data is present besides the general data points
        val relevantDimensions = dataCompositionService.getRelevantDataPointTypes(dataDimension.dataType) - ignoredDataPointTypes
        val couldBeAssembled = relevantDimensions.any { isDataPointDeliverable(dataDimension.toBasicDataPointDimensions(it)) }

        return activeDataPoint != null || activeDataset != null || couldBeAssembled
    }

    fun isDataPointDeliverable(dataDimension: BasicDataPointDimensions): Boolean {
        val dataPointMetainformationEntity =
            // how to deal with inactive data points?
            dataPointMetaInformationRepository.findByDataPointTypeInAndCompanyIdAndReportingPeriodAndCurrentlyActiveTrue(
                dataPointTypes = setOf(dataDimension.dataPointType),
                companyId = dataDimension.companyId,
                reportingPeriod = dataDimension.reportingPeriod,
            )
        return dataPointMetainformationEntity.isNotEmpty() &&
                dataPointMetainformationEntity.first().isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())
    }
}*/
