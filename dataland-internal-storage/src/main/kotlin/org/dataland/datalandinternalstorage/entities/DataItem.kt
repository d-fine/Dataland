package org.dataland.datalandinternalstorage.entities

import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "data_items")
data class DataItem(
    @Id
    @Column(name = "data_id")
    val id: String,
    @Column(name = "correlation_id")
    val correlationId: String,
    @Column(name = "data")
    val data: String
)