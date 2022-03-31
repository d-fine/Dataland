package org.dataland.datalandbackend.model

/**
 * Required input for data manager to get valid data from data store
 * @param dataId unique identifier to identify data in the data store
 * @param dataType expected type of the data
 */
data class DataManagerInputToGetData(
    val dataId: String,
    val dataType: String
)
