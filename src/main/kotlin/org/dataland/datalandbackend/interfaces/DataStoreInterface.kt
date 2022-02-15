package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

@Component
interface DataStoreInterface {
    fun addDataSet(dataSet: DataSet): DataSetMetaInformation

    fun listDataSets(): List<DataSetMetaInformation>

    fun getDataSet(id: String): DataSet
}
