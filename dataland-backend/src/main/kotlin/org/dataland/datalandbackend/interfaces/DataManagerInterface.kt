package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.StoredDataSet
import org.springframework.stereotype.Component
// TODO Rewrite/Adjust docs
/**
 * Defines the required functionalities for a data store
 */
@Component
interface DataManagerInterface {
    /**
     * Method to add a data set to the data store
     * @param storedDataSet contains
     * companyId ID of the company the data belongs to
     * dataType as the type of data (e.g. EU Taxonomy)
     * data as data to be stored in the data store
     * @return ID of the newly created data store entry
     */
    fun addDataSet(storedDataSet: StoredDataSet): String
/*
    /**
     * Method to list the meta information of all data sets in the data store
     * @return list of meta information of all data sets in the data store
     */
    fun listDataSets(): List<DataSetMetaInformation>
*/
    /**
     * Method to get the data of a single entry in the data store
     * @param dataIdentifier identifier of the stored data (consists of data id and data type)
     * @return all data associated to the identifier provided
     */
    fun getDataSet(dataIdentifier: DataIdentifier): String

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

    fun getCompanyById(companyId: String): CompanyMetaInformation
}
