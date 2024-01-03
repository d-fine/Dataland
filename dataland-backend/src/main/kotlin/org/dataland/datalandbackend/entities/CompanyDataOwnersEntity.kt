package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * The entity storing data ownership regarding a company stored in dataland
 */
@Entity
@Table(name = "data_owners_for_companies")
data class CompanyDataOwnersEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @ElementCollection
    @Column(name = "data_owners")
    val dataOwners: MutableList<String>,
)
