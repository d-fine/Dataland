package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType

interface DataMetaInformationManagerInterface {
    /**
     * Method to make the data manager search for meta info
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @return a list of meta info about data depending on the filters:
     */
    fun searchDataMetaInfo(companyId: String = "", dataType: DataType? = null): List<DataMetaInformationEntity>

    /**
     * Method to make the data manager get meta info about one specific data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity

    /**
     * Method to associate data information with a specific company
     * @param company The company to associate the data meta information with
     * @param dataId The id of the dataset to associate with the company
     * @param dataType The dataType of the dataId
     */
    fun storeDataMetaInformation(company: StoredCompanyEntity, dataId: String, dataType: DataType): DataMetaInformationEntity
}