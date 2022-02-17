package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<Int, DataSet>()
    private var counter = 0

    override fun addDataSet(dataSet: DataSet): DataSetMetaInformation {
        data[counter] = dataSet
        counter++
        return DataSetMetaInformation(name = dataSet.name, id = counter.toString())
    }

    override fun listDataSets(): List<DataSetMetaInformation> {
        return data.map { DataSetMetaInformation(name = it.value.name, id = it.key.toString()) }
    }

    override fun getDataSet(id: String): DataSet {
        return data[id.toInt()] ?: throw IllegalArgumentException("The id: $id does not exist.")
    }
}
