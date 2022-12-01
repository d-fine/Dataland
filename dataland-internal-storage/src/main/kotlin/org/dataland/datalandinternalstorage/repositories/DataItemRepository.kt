package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataItem
import org.springframework.data.mongodb.repository.MongoRepository

interface DataItemRepository : MongoRepository<DataItem, String> {
}