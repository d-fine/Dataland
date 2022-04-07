package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StoredCompany

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
     * Method to add a company
     * @param companyInformation denotes information of the company
     * @return information of the newly created entry in the company data store of Dataland,
     * including the generated company ID
     */
    fun addCompany(companyInformation: CompanyInformation): StoredCompany

    /**
     * Method to list matching companies in Dataland by searching for a company name
     * @param companyName string used for substring matching against the companyNames of all entries
     * @return list of all matching companies in Dataland
     */
    fun listCompanies(companyName: String, wildcardSearch: String): List<StoredCompany>

    /**
     * Method to retrieve information about a specific company
     * @param companyId
     * @return information about the retrieved company
     */
    fun getCompanyById(companyId: String): StoredCompany
}
