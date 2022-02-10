package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.Identifier
import org.springframework.stereotype.Component

@Component
interface DataStoreInterface {
    fun addDataSet(dataSet: DataSet): Identifier {
        throw NotImplementedError("Adding of data sets is not implemented.")
    }

    fun listDataSets(): List<Identifier> {
        throw NotImplementedError("Listing of data sets is not implemented.")
    }

    fun getDataSet(id: String): DataSet {
        throw NotImplementedError("Retrieval of data sets is not implemented.")
    }
}
