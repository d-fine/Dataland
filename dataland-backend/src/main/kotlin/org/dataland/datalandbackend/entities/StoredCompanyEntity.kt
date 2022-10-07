package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * The entity storing data regarding a company stored in dataland
 */
@Entity
@Table(name = "stored_companies")
data class StoredCompanyEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @Column(name = "company_name")
    var companyName: String,

    @ElementCollection
    @Column(name = "company_alternative_names")
    var companyAlternativeNames: List<String>?,

    @Column(name = "headquarters")
    var headquarters: String,

    @Column(name = "sector")
    var sector: String,

    @OneToMany(mappedBy = "company")
    var identifiers: MutableList<CompanyIdentifierEntity>,

    @OneToMany(mappedBy = "company")
    val dataRegisteredByDataland: MutableList<DataMetaInformationEntity>,

    @Column(name = "country_code")
    var countryCode: String,

    @Column(name = "is_teaser_company")
    var isTeaserCompany: Boolean
) : ApiModelConversion<StoredCompany> {
    @JsonValue
    override fun toApiModel(): StoredCompany {
        return StoredCompany(
            companyId = companyId,
            companyInformation = CompanyInformation(
                companyName = companyName,
                companyAlternativeNames = companyAlternativeNames,
                headquarters = headquarters,
                sector = sector,
                identifiers = identifiers.map { it.toApiModel() }.toList(),
                countryCode = countryCode,
                isTeaserCompany = isTeaserCompany,
            ),
            dataRegisteredByDataland = dataRegisteredByDataland.map { it.toApiModel() }.toMutableList()
        )
    }
}
