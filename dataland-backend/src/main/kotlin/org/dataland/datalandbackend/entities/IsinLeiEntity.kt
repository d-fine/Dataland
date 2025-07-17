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
    indexes = [Index(name = "idx_company_id", columnList = "company_id", unique = false)],
)
data class IsinLeiEntity(
    @Column(name = "company_id", nullable = false, unique = false)
    val companyId: String,
    @Id
    @Column(name = "isin", nullable = false, unique = true)
    val isin: String,
    @Column(name = "lei", nullable = true, unique = false)
    val lei: String?,
)
