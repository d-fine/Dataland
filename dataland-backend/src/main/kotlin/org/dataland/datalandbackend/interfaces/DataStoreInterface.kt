package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet

/**
 * Defines the required functionalities for a data store
 */
interface DataStoreInterface {
    /**
     * Method to add a data set to the data store
     * @param storableDataSet contains
     * companyId ID of the company the data belongs to
     * dataType as the type of data (e.g. EU Taxonomy)
     * data as data to be stored in the data store
     * @return ID of the newly created data store entry
     */
    fun addDataSet(storableDataSet: StorableDataSet): String

    /**
     * Method to list the meta information of all data sets in the data store
     * @return list of meta information of all data sets in the data store
     */
    fun listDataSets(): List<DataSetMetaInformation>

    /**
     * Method to get the data of a single entry in the data store
     * @param dataIdentifier identifier of the stored data (consists of data id and data type)
     * @return all data associated to the identifier provided
     */
    fun getStorableDataSet(dataIdentifier: DataIdentifier): StorableDataSet

    /**
     * Method to add a company to the data store
     * @param companyName name of the company to be stored in the data store
     * @return meta information of the newly created entry in the data store (companyId and companyName)
     */
    fun addCompany(companyName: String): CompanyMetaInformation

    /**
     * Method to list matching companies in the data store by searching for a name
     * @param name string used for substring matching against the companyName of all entries
     * @return list of all matching companies in the data store
     */
    fun listCompaniesByName(name: String): List<CompanyMetaInformation>

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
