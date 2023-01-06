package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import java.time.Instant

/**
 * --- Non-API model ---
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param companyId identifies the company for which a data set is to be stored
 * @param dataType the type of the data set
 * @param data the actual data
 */
data class StorableDataSet(
    val companyId: String,
    val dataType: DataType,
    val uploaderUserId: String,
    val uploadTime: Instant,
    val data: String
) {
    fun requireConsistencyWith(metaDataEntry: DataMetaInformationEntity) {
        if (dataType != DataType.valueOf(metaDataEntry.dataType)
            || uploaderUserId != metaDataEntry.uploaderUserId
            || uploadTime != metaDataEntry.uploadTime) {
            throw InternalServerErrorApiException(
                "Dataland-Internal inconsistency regarding dataset ${metaDataEntry.dataId}",
                "We are having some internal issues with the dataset ${metaDataEntry.dataId}, please contact support.",
                "The meta-data of dataset ${metaDataEntry.dataId} differs between the data store and the backend database"
            )
        }

    }
}
