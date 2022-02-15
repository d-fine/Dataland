package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

@Component
interface DataStoreInterface {
    fun addDataSet(dataSet: DataSet): DataSetMetaInformation {
        throw NotImplementedError("Adding of data sets is not implemented.")
    }

    fun listDataSets(): List<DataSetMetaInformation> {
        throw NotImplementedError("Listing of data sets is not implemented.")
    }

    fun getDataSet(id: String): DataSet {
        throw NotImplementedError("Retrieval of data sets is not implemented.")
    }
}
