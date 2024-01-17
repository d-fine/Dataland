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
interface ReducedCompanyEntity {
    val companyId: String

    val companyName: String

    val headquarters: String

    val sector: String?

    var permId: String?

    var search_rank: Int
}
