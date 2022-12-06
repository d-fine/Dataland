package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.interfaces.DataStoreInterface
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Simple implementation of a data store using a postgres database
 */
@Component
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository
) : DataStoreInterface {
    override fun insertDataSet(data: String): String {
        val dataID = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        dataItemRepository.save(DataItem(dataID, "TODO", data))
        return dataID
    }

    override fun selectDataSet(dataId: String): String {
        // TODO should not-found-data be "" or "{}" or something else
        return dataItemRepository.findById(dataId).orElse(DataItem("", "", "")).data
    }
}
