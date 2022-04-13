package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StoredCompany
import java.math.BigDecimal

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
    fun searchCompaniesByIndex(selectedIndex: CompanyInformation.StockIndex): List<StoredCompany>

    /**
     * Method to retrieve information about a specific company
     * @param companyId
     * @return information about the retrieved company
     */
    fun getCompanyById(companyId: String): StoredCompany

    /**
     * Method to retrieve the green asset ratio of one or all indices
     * @param selectedIndex index for which the green asset ratio is to be retrieved (all indices are retrieved if null)
     * @return green asset ratio in a map form
     */
    fun getGreenAssetRatio(selectedIndex: CompanyInformation.StockIndex?):
        Map<CompanyInformation.StockIndex, BigDecimal>
}
