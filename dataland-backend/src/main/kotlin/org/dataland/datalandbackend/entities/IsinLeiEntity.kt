package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * The database entity for mapping ISINs to LEIs.
 */
@Entity
@Table(name = "isin_lei_mapping")
data class IsinLeiEntity(
    @Id
    @Column(name = "isin", nullable = false, unique = true)
    val isin: String,
    @Column(name = "lei", nullable = false)
    val lei: String,
)
