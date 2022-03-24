package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
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
     * Method to make the data manager add a data set to a data store
     * @param storedDataSet contains all the inputs needed by Dataland to add a data set to a data store
     * @return ID of the newly created data store entry
     */
    fun addDataSet(storedDataSet: StorableDataSet): String

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataIdentifier identifier of the stored data (consists of data id and data type)
     * @return all data associated to the identifier provided
     */
    fun getDataSet(dataIdentifier: DataIdentifier): String

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
    fun getMetaData(dataId: String): DataSetMetaInformation

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    /**
     * Method to add a company to the meta data store
     * @param companyName name of the company to be stored in the meta data store
     * @return meta information of the newly created entry in the meta data store (companyId and companyName)
     */
    fun addCompany(companyName: String): CompanyMetaInformation

    /**
     * Method to list matching companies in the meta data store by searching for a company name
     * @param companyName string used for substring matching against the companyNames of all entries
     * @return list of all matching companies in the meta data store
     */
    fun listCompaniesByName(companyName: String): List<CompanyMetaInformation>

    /**
     * Method to list all existing data sets of a given company
     * @param companyId
     * @return list of all data set IDs along with their data type
     */
    fun listDataSetsByCompanyId(companyId: String): List<DataIdentifier>

    /**
     * Method to retrieve meta information about a specific company
     * @param companyId
     * @return meta information consisting of company Id and company name
     */
    fun getCompanyById(companyId: String): CompanyMetaInformation
}
