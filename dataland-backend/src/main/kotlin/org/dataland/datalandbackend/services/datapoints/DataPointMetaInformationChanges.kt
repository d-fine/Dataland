package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Component
class DataPointMetaInformationChanges(
    @Autowired val dataPointMetaInformationManager: DataPointMetaInformationManager,
    @Autowired private val dataPointMetaInformationRepositoryInterface: DataPointMetaInformationRepository,
) {
    /**
     * Method to store data point meta information
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
        val dataPointMetaInformation = dataPointMetaInformationManager.getDataPointMetaInformationByDataId(dataId)
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
        val dataPointMetaInformation = dataPointMetaInformationManager.getDataPointMetaInformationByDataId(dataId)
        dataPointMetaInformation.currentlyActive = newCurrentlyActiveValue
        dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)
    }
}
