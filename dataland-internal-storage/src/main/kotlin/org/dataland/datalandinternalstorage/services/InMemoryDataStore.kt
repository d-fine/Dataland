package org.dataland.datalandinternalstorage.service

import org.dataland.datalandinternalstorage.interfaces.DataStoreInterface
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple implementation of a data store using in memory storage
 */
@Component
class InMemoryDataStore : DataStoreInterface {
    var storedData: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    override fun insertDataSet(data: String): String {
        val dataID = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        storedData[dataID] = data
        return dataID
    }

    override fun selectDataSet(dataId: String): String {
        return storedData[dataId] ?: ""
    }
}
