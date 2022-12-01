package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataItem
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Component

interface DataItemRepository : MongoRepository<DataItem, String> {
    @Query("{id: '?0'}")
    fun findItemById(id: String): DataItem
}