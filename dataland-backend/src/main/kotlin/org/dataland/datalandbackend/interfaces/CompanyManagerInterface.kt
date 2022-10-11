package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType

/**
 * Defines the required functionalities for the Dataland company manager
 */
interface CompanyManagerInterface {

    /**
     * Method to add a company
     * @param companyInformation denotes information of the company
     * @return information of the newly created entry in the company data store of Dataland,
     * including the generated company ID
     */
    fun addCompany(companyInformation: CompanyInformation): StoredCompanyEntity

    /**
     * Method to search for companies matching the company name or identifier
     * @param searchString string used for substring matching against the company name and/or identifiers
     * @param onlyCompanyNames boolean determining if the search should be solely against the company names
     * @param dataTypeFilter if not empty, return only companies that have data reported for
     * one of the specified dataTypes
     * @param countryCodeFilter set of strings with ISO country codes to return companies whose headquarters are in
     * the country of one of those ISO country codes
     * @param sectorFilter set of strings with sector names to return companies which operate in one of those sectors
     * @return list of all matching companies in Dataland
     */
    fun searchCompanies(
        searchString: String,
        onlyCompanyNames: Boolean,
        dataTypeFilter: Set<DataType>,
        countryCodeFilter: Set<String>,
        sectorFilter: Set<String>,
    ): List<StoredCompanyEntity>

    /**
     * Method to retrieve information about a specific company
     * @param companyId
     * @return information about the retrieved company
     */
    fun getCompanyById(companyId: String): StoredCompanyEntity

    /**
     * Returns a list of available country codes across all stored companies
     */
    fun getDistinctCountryCodes(): Set<String>

    /**
     * Returns a list of available sectors across all stored companies
     */
    fun getDistinctSectors(): Set<String>

    /**
     * Method to retrieve the list of currently set teaser company IDs
     * @return a list of company IDs that are currently labeled as teaser companies
     */
    fun getTeaserCompanyIds(): List<String>

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
}
