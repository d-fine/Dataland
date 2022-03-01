package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

/**
 * Defines the required functionalities for a data store
 */
@Component
interface DataStoreInterface {
    /**
     * Method to add a data set to the data store
     * @param dataSet data to be stored in the data store
     * @return meta information of the newly created entry in the data store (id and name)
     */
    fun addDataSet(dataSet: DataSet): DataSetMetaInformation

    /**
     * Method to list the meta information of all data sets in the data store
     * @return list of meta information of all data sets in the data store
     */
    fun listDataSets(): List<DataSetMetaInformation>

    /**
     * Method to get the values of a single data set
     * @param id identifier of the stored data set
     * @return all data associated to the identifier provided
     */
    fun getDataSet(id: String): DataSet
}
