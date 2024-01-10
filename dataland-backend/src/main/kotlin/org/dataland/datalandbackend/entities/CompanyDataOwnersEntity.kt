package org.dataland.datalandbackend.entities

import jakarta.persistence.*

/**
 * The entity storing data ownership regarding a company stored in dataland
 */
@Entity
@Table(name = "data_owners_for_companies")
data class CompanyDataOwnersEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name = "data_owners")
    val dataOwners: MutableList<String>,
)
