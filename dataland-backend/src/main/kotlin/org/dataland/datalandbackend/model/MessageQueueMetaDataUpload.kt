package org.dataland.datalandbackend.model

import kotlinx.serialization.Serializable

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param dataType type of the data
 * @param company unique identifier to identify the company the data is associated with
 */
@Serializable
data class MessageQueueMetaDataUpload(
    val dataId: String,
    val storableDataSet: StorableDataSet,
)
