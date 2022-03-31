package org.dataland.datalandbackend.model

/**
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param companyId identifies the company for which a data set is to be stored
 * @param dataType the type of the data set
 * @param data the actual data
 */
data class StorableDataSet(
    val companyId: String,
    val dataType: String,
    val data: String
)
