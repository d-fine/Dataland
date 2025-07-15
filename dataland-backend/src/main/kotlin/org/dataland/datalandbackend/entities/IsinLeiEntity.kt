package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * The database entity for mapping ISINs to LEIs.
 */
@Entity
@Table(
    name = "isin_lei_mapping",
    indexes = [Index(name = "idx_isin", columnList = "isin", unique = true)],
)
data class IsinLeiEntity(
    @Id
    @Column(name = "company_id", nullable = false, unique = true)
    val companyId: String,
    @Column(name = "isin", nullable = false, unique = true)
    val isin: String,
    @Column(name = "lei", nullable = true, unique = true)
    val lei: String?,
)
