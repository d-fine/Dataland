package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.StorableDataSet

/**
 * Defines the required functionalities to the data store and save meta data in the Dataland-Meta-Data-Storage
 */
interface DataStoreInterface {
    /**
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    fun addDataSet(storableDataSet: StorableDataSet): String

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataManagerInputToGetData contains all the inputs needed by Dataland
     * @return data associated with the data ID provided in the input
     */
    fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String
}
