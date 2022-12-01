package org.dataland.datalandinternalstorage.service

import org.dataland.datalandinternalstorage.interfaces.DataStoreInterface
import org.springframework.stereotype.Component
import java.util.UUID
import org.bson.types.ObjectId
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
 * Simple implementation of a data store using a mongo database
 */
@Component
@EnableMongoRepositories("org.dataland.datalandinternalstorage.repositories")
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository
) : DataStoreInterface {
    override fun insertDataSet(data: String): String {
        val dataID = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        dataItemRepository.save(DataItem(dataID, "TODO", data))
        return dataID
    }

    override fun selectDataSet(dataId: String): String {
        return dataItemRepository.findItemById(dataId).data
    }
}