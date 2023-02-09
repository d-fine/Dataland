package org.dataland.datalandinternalstorage.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

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
    val data: String,
)
