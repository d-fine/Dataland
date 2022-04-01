package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet

/**
 * Defines the required functionalities to process data for data manager
 */
interface DataProcessorInterface {
    /**
     * Method to make the data manager search for meta info
     * @param dataId if not empty, it filters the requested meta info to one specific data ID
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @return a list of meta info about data depending on the filters:
     * It contains only one element, if a specific dataId was given as input
     */
    fun searchDataMetaInfo(
        dataId: String = "",
        companyId: String = "",
        dataType: String = ""
    ): List<DataMetaInformation>
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
