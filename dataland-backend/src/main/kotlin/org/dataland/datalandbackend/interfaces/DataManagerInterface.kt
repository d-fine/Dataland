package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.PostCompanyRequestBody
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
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    fun addDataSet(storableDataSet: StorableDataSet): String

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataManagerInputToGetData contains all the inputs needed by Dataland
     * @return data associated with the data ID provided in the input
     */
    fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String

    /*
    ________________________________
    Methods to process meta data:
    ________________________________
     */

    /**
     * Method to make the data manager search for meta info
     * @param dataId if not empty, it filters the requested meta info to one specific data ID
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @return a list of meta info about data depending on the filters:
     * It contains only one element, if a specific dataId was given as input
     */
    fun searchDataMetaInfo(
        dataId: String = "",
        companyId: String = "",
        dataType: String = ""
    ): List<DataMetaInformation>

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    /**
     * Method to add a company
     * @param companyName name of the company to be stored
     * @param headquarters city where the headquarters of the company is located
     * @param industrialSector sector in which the company operates
     * @param marketCap For publicly traded companies: The total monetary value of the
     * outstanding shares of the company
     * @param reportingDateOfMarketCap date to which the market cap value refers
     * @return meta information of the newly created entry in the company data store of Dataland
     */
    fun addCompany(postCompanyRequestBody: PostCompanyRequestBody): CompanyMetaInformation

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
