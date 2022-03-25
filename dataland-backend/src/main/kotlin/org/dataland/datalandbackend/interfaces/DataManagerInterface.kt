package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet

/**
 * Defines the required functionalities for the Dataland data manager
 */
interface DataManagerInterface {

    /*
    ________________________________
    Methods to route data inserts and queries to the data store and save meta data in the Dataland-Meta-Data-Storage:
    ________________________________
     */

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

    /*
    ________________________________
    Methods to process meta data:
    ________________________________
     */

    /**
     * Method to make the data manager get meta info associated with a data ID
     * @param dataId
     * @return all meta data associated with the data behind the data ID
     */
    fun getMetaData(dataId: String): DataMetaInformation

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    /**
     * Method to add a company
     * @param companyName name of the company to be stored
     * @return meta information of the newly created entry in the company data store of Dataland
     */
    fun addCompany(companyName: String): CompanyMetaInformation

    /**
     * Method to list matching companies in Dataland by searching for a company name
     * @param companyName string used for substring matching against the companyNames of all entries
     * @return list of all matching companies in Dataland
     */
    fun listCompaniesByName(companyName: String): List<CompanyMetaInformation>
/*

This method will be obsolete as soon as AllDataAPI is implemented.

    /**
     * Method to list all existing data sets of a given company
     * @param companyId
     * @return list of all data set IDs along with their data type
     */
    fun listDataSetsByCompanyId(companyId: String): List<DataManagerInputToGetData>
*/
    /**
     * Method to retrieve meta information about a specific company
     * @param companyId
     * @return meta information about the retrieved company
     */
    fun getCompanyById(companyId: String): CompanyMetaInformation
}
