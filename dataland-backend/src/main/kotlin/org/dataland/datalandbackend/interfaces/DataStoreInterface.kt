package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StoredDataSet
import org.springframework.stereotype.Component

/**
 * Defines the required functionalities for a data store
 */
@Component
interface DataStoreInterface {
    /**
     * Method to add a data set to the data store
     * @param storedDataSet contains
     * companyId ID of the company the data belongs to
     * dataType as the type of data (e.g. EU Taxonomy)
     * data as data to be stored in the data store
     * @return ID of the newly created data store entry
     */
    fun addDataSet(storedDataSet: StoredDataSet): String

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
    fun getDataSet(dataIdentifier: DataIdentifier): String

    /**
     * Method to add a company to the data store
     * @param companyName name of the company to be stored in the data store
     * @return meta information of the newly created entry in the data store (companyId and companyName)
     */
    fun addCompany(companyName: String): CompanyMetaInformation

    /**
     * Method to list all companies in the data store
     * @return list of all company names along with their respective IDs in the data store
     */
    fun listAllCompanies(): List<CompanyMetaInformation>

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

    fun getCompanyById(companyId: String): CompanyMetaInformation
}
