package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

/**
 * Service to determine if data is available
 */
@Service("DataAvailabilityChecker")
class DataAvailabilityChecker
    @Autowired
    constructor(
        private val datasetMetaInformationRepository: DataMetaInformationRepository,
        private val dataPointMetaInformationRepository: DataPointMetaInformationRepository,
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val dataCompositionService: DataCompositionService,
        private val dataPointUtils: DataPointUtils,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

    fun getMetaDataOfActiveDatasets(dataDimensions: List<BasicDataDimensions>): List<DataMetaInformation> {
        val searchTriples = dataDimensions.map { Triple(it.companyId, it.dataType, it.reportingPeriod) }
        return datasetMetaInformationRepository.findByDataDimensionsTriples(searchTriples).map { it.toApiModel() }
    }



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
    }
