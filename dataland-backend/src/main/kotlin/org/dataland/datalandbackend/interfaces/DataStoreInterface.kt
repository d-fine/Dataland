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
    fun insertDataSet(data: String): String

    /**
     * Method to get the data of a single entry in the data store
     * @param dataIdentifier identifier of the stored data (consists of data id and data type)
     * @return all data associated to the identifier provided
     */
    fun selectDataSet(dataId: String): String
}
