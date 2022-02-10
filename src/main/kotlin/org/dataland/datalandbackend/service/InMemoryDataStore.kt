package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.Identifier
import org.springframework.stereotype.Component

@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<Int, DataSet>()
    private var counter = 0

    override fun addDataSet(dataSet: DataSet): Identifier {
        data[counter] = dataSet
        counter++
        return Identifier(name = dataSet.name, id = (counter - 1).toString())
    }

    override fun listDataSets(): List<Identifier> {
        val content: MutableList<Identifier> = mutableListOf()
        for (key in data.keys)
            content.add(Identifier(name = data[key]?.name ?: "Unknown", id = key.toString()))
        return content
    }

    override fun getDataSet(id: String): DataSet {
        return data[id.toInt()] ?: throw IllegalArgumentException("The $id does not exist.")
    }
}
