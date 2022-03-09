package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.Company
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

/**
 * Defines the required functionalities for a data store
 */
@Component
interface DataStoreInterface {
    /**
     * Method to add a data set to the data store
     * @param companyId ID of the company the data belongs to
     * @param dataType type of data (e.g. EU Taxonomy)
     * @param data data to be stored in the data store
     * @return ID of the newly created data store entry
     */
    fun addDataSet(companyId: String, dataType: String, data: String): String

    /**
     * Method to list the meta information of all data sets in the data store
     * @return list of meta information of all data sets in the data store
     */
    fun listDataSets(): List<DataSetMetaInformation>

    /**
     * Method to get the data of a single entry in the data store
     * @param dataId identifier of the stored data
     * @return all data associated to the identifier provided
     */
    fun getDataSet(dataId: String, dataType: String): String

    /**
     * Method to add a company to the data store
     * @param companyName name of the company to be stored in the data store
     * @return meta information of the newly created entry in the data store (companyId and companyName)
     */
    fun addCompany(companyName: String): CompanyMetaInformation

    /**
     * Method to list all companies in the data store
     * @return list of all companies in the data store
     */
    fun listAllCompanies(): Map<String, Company>

    /**
     * Method to list matching companies in the data store by searching for a name
     * @param name string used for substring matching against the companyName of all entries
     * @return map of all matching companies in the data store
     */
    fun listCompaniesByName(name: String): Map<String, Company>
}
