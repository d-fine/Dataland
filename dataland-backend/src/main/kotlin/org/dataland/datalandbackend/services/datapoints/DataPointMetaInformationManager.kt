package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Service
class DataPointMetaInformationManager
    @Autowired
    constructor(
        private val dataPointMetaInformationRepositoryInterface: DataPointMetaInformationRepository,
    ) {
        /**
         * Get meta info about one specific data point
         * @param dataPointId filters the requested meta info to one specific data ID
         * @return meta info about data behind the dataId
         */
        @Transactional(readOnly = true)
        fun getDataPointMetaInformationById(dataPointId: String): DataPointMetaInformationEntity =
            dataPointMetaInformationRepositoryInterface.findById(dataPointId).orElseThrow {
                ResourceNotFoundApiException(
                    "Data point not found",
                    "No data point with the id: $dataPointId could be found in the data store.",
                )
            }

        /**
         * Get the currently active data id for a specific set of data point dimensions
         * @param dataPointDimensions the data point dimensions to get the currently active data id for
         * @return the id of the currently active data point
         */
        @Transactional(readOnly = true)
        fun getCurrentlyActiveDataId(dataPointDimensions: BasicDataPointDimensions): String? =
            dataPointMetaInformationRepositoryInterface.getActiveDataPointId(dataPointDimensions)

        /**
         * Method to get the data point dimensions from a data id
         * @param dataPointId the id of the data point
         * @return the data point dimensions
         */
        @Transactional(readOnly = true)
        fun getDataPointDimensionFromId(dataPointId: String): BasicDataPointDimensions {
            val dataPointMetaInformation = getDataPointMetaInformationById(dataPointId)
            return BasicDataPointDimensions(
                reportingPeriod = dataPointMetaInformation.reportingPeriod,
                companyId = dataPointMetaInformation.companyId,
                dataPointType = dataPointMetaInformation.dataPointType,
            )
        }

        /**
         * Method to store the meta information of a data point
         * @param dataPointMetaInformation the meta information to store
         * @return the stored meta information
         */
        @Transactional
        fun storeDataPointMetaInformation(dataPointMetaInformation: DataPointMetaInformationEntity): DataPointMetaInformationEntity =
            dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)

        /**
         * Method to update the QA status of a data point
         * @param dataPointId the id of the data point to update
         * @param newQaStatus the new value for the QA status
         */
        @Transactional
        fun updateQaStatusOfDataPoint(
            dataPointId: String,
            newQaStatus: QaStatus,
        ) {
            val dataPointMetaInformation = getDataPointMetaInformationById(dataPointId)
            dataPointMetaInformation.qaStatus = newQaStatus
            dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)
        }

        /**
         * Method to update the currently active flag of a data point
         * @param dataPointId the id of the data point to update
         * @param newCurrentlyActiveValue the new value for the currently active flag
         */
        @Transactional
        fun updateCurrentlyActiveFlagOfDataPoint(
            dataPointId: String?,
            newCurrentlyActiveValue: Boolean?,
        ) {
            if (dataPointId == null) {
                return
            }
            require(newCurrentlyActiveValue != false) { "Currently active can only be true or null due to a constraint in the data base." }
            val dataPointMetaInformation = getDataPointMetaInformationById(dataPointId)
            dataPointMetaInformation.currentlyActive = newCurrentlyActiveValue
            dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)
        }

        /**
         * Method to get the reporting periods with active data points for a specific set of data point types and one company
         * @param dataPointTypes the data point types to filter for
         * @param companyId the company to filter for
         * @return the reporting periods with at least one active data point
         */
        fun getReportingPeriodsWithActiveDataPoints(
            dataPointTypes: Set<String>,
            companyId: String,
        ): Set<String> =
            dataPointMetaInformationRepositoryInterface
                .findByDataPointTypeInAndCompanyIdAndCurrentlyActiveTrue(
                    dataPointTypes = dataPointTypes,
                    companyId = companyId,
                ).map { it.reportingPeriod }
                .toSet()
    }
