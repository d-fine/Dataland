package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.StockIndex

/**
 * Defines the required functionalities for the Dataland data manager
 */
interface CompanyManagerInterface {

    /**
     * Method to add a company
     * @param companyInformation denotes information of the company
     * @return information of the newly created entry in the company data store of Dataland,
     * including the generated company ID
     */
    fun addCompany(companyInformation: CompanyInformation): StoredCompany

    /**
     * Method to search for companies matching the company name or identifier
     * @param searchString string used for substring matching against the company name and/or identifiers
     * @param onlyCompanyNames boolean determining if the search should be solely against the company names
     * @return list of all matching companies in Dataland
     */
    fun searchCompanies(searchString: String, onlyCompanyNames: Boolean): List<StoredCompany>

    /**
     * Method to search for companies in Dataland that are contained in the specified stock index
     * @param selectedIndex string used to filter against a stock index
     * @return list of all matching companies in Dataland
     */
    fun searchCompaniesByIndex(selectedIndex: StockIndex): List<StoredCompany>

    /**
     * Method to retrieve information about a specific company
     * @param companyId
     * @return information about the retrieved company
     */
    fun getCompanyById(companyId: String): StoredCompany

    /**
     * Method to set a list of teaser companies
     * @param companyIds the list of company IDs to be used as teaser companies
     */
    fun setTeaserCompanies(companyIds: List<String>)

    /**
     * Method to check if a company is a teaser company and hence publicly available
     * @param companyId the ID of the company to be checked
     * @return a boolean signalling if the company is public or not
     */
    fun isCompanyPublic(companyId: String): Boolean

    /**
     * Method to verify that a given company exists in the company store
     * @param companyId the ID of the to be verified company
     */
    fun verifyCompanyIdExists(companyId: String)

    /**
     * Method to add the data meta information to a company in the company store
     * @param companyId the ID of the company the data belong to
     * @param dataMetaInformation the information to be linked to the company in the store
     */
    fun addMetaDataInformationToCompanyStore(companyId: String, dataMetaInformation: DataMetaInformation)
    fun getTeaserCompanyIds(): List<String>
}
