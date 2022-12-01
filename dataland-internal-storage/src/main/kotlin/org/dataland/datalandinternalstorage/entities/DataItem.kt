package org.dataland.datalandinternalstorage.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("data")
class DataItem(
    @field:Id
    val id: String,
    val correlationId: String,
    val data: org.bson.Document
)