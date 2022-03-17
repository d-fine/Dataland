package org.dataland.datalandbackend.interfaces

import org.springframework.stereotype.Component

/**
 * Defines the required functionalities for a in-memory data store
 */
@Component
interface DataStoreInterface {
    /**
     * Method to insert data into to the data store
     * @param data contains the data to be stored in the data store
     * @return ID of the newly created data store entry
     */
    fun insertDataSet(data: String): String

    /**
     * Method to get the data of a single entry in the data store
     * @param dataId identifier of the stored data
     * @return all data associated to the dataId provided
     */
    fun selectDataSet(dataId: String): String
}
