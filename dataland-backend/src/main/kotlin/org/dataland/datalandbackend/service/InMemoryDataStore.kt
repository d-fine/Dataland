package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<String, String>()
    private var dataCounter = 0

    override fun insertDataSet(data: String): String {
        dataCounter++
        this.data["$dataCounter"] = data
        return "$dataCounter"
    }

    override fun selectDataSet(dataId: String): String {
        return data[dataId] ?: ""
    }
}
