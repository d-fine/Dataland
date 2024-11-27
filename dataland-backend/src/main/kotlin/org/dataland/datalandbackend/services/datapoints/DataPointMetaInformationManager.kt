package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DataPointDimension
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A service class for managing data meta-information
 */
@Component
class DataPointMetaInformationManager(
    @Autowired private val dataPointMetaInformationRepositoryInterface: DataPointMetaInformationRepository,
    @Autowired val dataPointMetaInformationChanges: DataPointMetaInformationChanges,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
     * Method to update the currently active data point for a specific data point dimension
     * @param dataPointDimension the data point dimension to update the currently active data point for
     * @param newActiveDataId the id of the new active data point
     * @param correlationId the correlation id for the operation
     */
    fun updateCurrentlyActiveDataPoint(
        dataPointDimension: DataPointDimension,
        newActiveDataId: String?,
        correlationId: String,
    ) {
        val currentlyActiveDataId = getCurrentlyActiveDataId(dataPointDimension)
        if (newActiveDataId.isNullOrEmpty() && !currentlyActiveDataId.isNullOrEmpty()) {
            logger.info("Setting data point with dataId $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
            dataPointMetaInformationChanges.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, false)
        } else if (newActiveDataId != currentlyActiveDataId && !newActiveDataId.isNullOrEmpty() && !currentlyActiveDataId.isNullOrEmpty()) {
            logger.info("Setting $newActiveDataId to active and $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
            dataPointMetaInformationChanges.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, false)
            dataPointMetaInformationChanges.updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
        } else {
            logger.info("No update of the currently active flag required (correlation ID: $correlationId).")
        }
    }
}
