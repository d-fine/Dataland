package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * The entity storing data regarding a company stored in dataland
 */
data class ReducedCompanyEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @Column(name = "company_name")
    var companyName: String,

    @Column(name = "headquarters")
    var headquarters: String,

    @Column(name = "sector")
    var sector: String?,

    @Column(name = "perm_id")
    var permId: String?,
)
