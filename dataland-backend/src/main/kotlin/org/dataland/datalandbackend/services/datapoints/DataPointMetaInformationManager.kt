package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DataPointDimension
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Component
class DataPointMetaInformationManager(
    @Autowired private val dataPointMetaInformationRepositoryInterface: DataPointMetaInformationRepository,
) {
    /**
     * Method to make the data manager get meta info about one specific data point
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataPointMetaInformationByDataId(dataId: String): DataPointMetaInformationEntity =
        dataPointMetaInformationRepositoryInterface.findById(dataId).orElseThrow {
            ResourceNotFoundApiException(
                "Data point not found",
                "No data point with the id: $dataId could be found in the data store.",
            )
        }

    /**
     * Method to get the currently active data id for a specific data point dimension
     * @param dataPointDimension the data point dimension to get the currently active data id for
     * @return the id of the currently active data point
     */
    fun getCurrentlyActiveDataId(dataPointDimension: DataPointDimension): String? =
        dataPointMetaInformationRepositoryInterface.getActiveDataPointId(dataPointDimension)

    /**
     * Method to get the data point dimension from a data id
     * @param dataId the id of the data point
     * @return the data point dimension
     */
    fun getDataPointDimensionFromId(dataId: String): DataPointDimension {
        val dataPointMetaInformation = getDataPointMetaInformationByDataId(dataId)
        return DataPointDimension(
            reportingPeriod = dataPointMetaInformation.reportingPeriod,
            companyId = dataPointMetaInformation.companyId,
            dataPointIdentifier = dataPointMetaInformation.dataPointIdentifier,
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
     * @param dataId the id of the data point to update
     * @param newQaStatus the new value for the QA status
     */
    @Transactional
    fun updateQaStatusOfDataPoint(
        dataId: String,
        newQaStatus: QaStatus,
    ) {
        val dataPointMetaInformation = getDataPointMetaInformationByDataId(dataId)
        dataPointMetaInformation.qaStatus = newQaStatus
        dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)
    }

    /**
     * Method to update the currently active flag of a data point
     * @param dataId the id of the data point to update
     * @param newCurrentlyActiveValue the new value for the currently active flag
     */
    @Transactional
    fun updateCurrentlyActiveFlagOfDataPoint(
        dataId: String,
        newCurrentlyActiveValue: Boolean,
    ) {
        val dataPointMetaInformation = getDataPointMetaInformationByDataId(dataId)
        dataPointMetaInformation.currentlyActive = newCurrentlyActiveValue
        dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)
    }
}
