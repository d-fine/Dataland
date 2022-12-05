package org.dataland.datalandinternalstorage.entities

import javax.persistence.*

@Entity
@Table(name = "data_items")
data class DataItem(
    @Id
    @Column(name = "data_id")
    val id: String,
    @Column(name = "correlation_id")
    val correlationId: String,
    @Column(name = "data", columnDefinition = "CLOB NOT NULL")
    @Lob
    val data: String
)
