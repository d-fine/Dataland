package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException

/**
 * --- Non-API model ---
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param companyId identifies the company for which a dataset is to be stored
 * @param dataType the type of the dataset
 * @param uploaderUserId the user ID of the user who requested the upload of this dataset
 * @param uploadTime is a timestamp for the upload of this dataset
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param data the actual data
 */

data class StorableDataset(
    val companyId: String,
    val dataType: DataType,
    val uploaderUserId: String,
    val uploadTime: Long,
    val reportingPeriod: String,
    val data: String,
) {
    /**
     * Checks the consistency of this dataset retrieved from the store
     * with the metadata stored in the Dataland database.
     * Throws an InternalServerErrorApiException if inconsistencies are detected
     */
    fun requireConsistencyWith(metaDataEntry: DataMetaInformationEntity) {
        if (dataType != DataType.valueOf(metaDataEntry.dataType) ||
            uploaderUserId != metaDataEntry.uploaderUserId ||
            uploadTime != metaDataEntry.uploadTime
        ) {
            throw InternalServerErrorApiException(
                "Dataland-Internal inconsistency regarding dataset ${metaDataEntry.dataId}",
                "We are having some internal issues with the dataset ${metaDataEntry.dataId}, please contact support.",
                "The meta-data of dataset ${metaDataEntry.dataId} differs between the data store and the database",
            )
        }
    }
}
