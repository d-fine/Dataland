package org.dataland.datalandinternalstorage.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * The database entity for storing company data
 */
@Entity
@Table(name = "data_items")
data class DataItem(
    @Id
    @Column(name = "data_id")
    val id: String,
    @Column(name = "data", columnDefinition = "TEXT")
    val data: String
)
