package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation

/**
 * Defines the required functionalities to add and retrieve company info and save associated company metadata in the
Dataland-Company-Meta-Data-Storage for the Dataland data manager
 */
interface CompanyDataStoreInterface {
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

    /**
     * Method to retrieve meta information about a specific company
     * @param companyId
     * @return meta information about the retrieved company
     */
    fun getCompanyById(companyId: String): CompanyMetaInformation
}
