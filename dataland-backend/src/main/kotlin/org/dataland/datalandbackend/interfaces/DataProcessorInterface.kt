package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataMetaInformation

/**
 * Defines the required functionalities to process data for data manager
 */
interface DataProcessorInterface {
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
}
