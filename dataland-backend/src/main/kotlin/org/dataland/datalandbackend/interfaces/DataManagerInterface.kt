package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.enums.StockIndex
import java.math.BigDecimal

/**
 * Defines the required functionalities for the Dataland data manager
 */
interface DataManagerInterface {

    /**
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    fun addDataSet(storableDataSet: StorableDataSet): String

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataId to identify the stored data
     * @param dataType to check the correctness of the type of the retrieved data
     * @return data set associated with the data ID provided in the input
     */
    fun getDataSet(dataId: String, dataType: String): StorableDataSet

    /**
     * Method to make the data manager search for meta info
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @return a list of meta info about data depending on the filters:
     */
    fun searchDataMetaInfo(companyId: String = "", dataType: String = ""): List<DataMetaInformation>

    /**
     * Method to make the data manager get meta info about one specific data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataMetaInfo(dataId: String): DataMetaInformation

    /**
     * Method to retrieve the green asset ratio of one or all indices
     * @param selectedIndex index for which the green asset ratio is to be retrieved (all indices are retrieved if null)
     * @return green asset ratio in a map form
     */
    fun getGreenAssetRatio(selectedIndex: StockIndex?): Map<StockIndex, BigDecimal>

    /**
     * Method to check if a data set belongs to a teaser company and hence is publicly available
     * @param dataId the ID of the data set to be checked
     * @return a boolean signalling if the data is public or not
     */
    fun isDataSetPublic(dataId: String): Boolean
}
