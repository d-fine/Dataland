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
    @OrderBy("asc")
    var companyAlternativeNames: List<String>?,
    @ElementCollection
    @Column(name = "company_contact_details")
    @OrderBy("asc")
    var companyContactDetails: List<String>?,
    @Column(name = "company_legal_form")
    var companyLegalForm: String?,
    @Column(name = "headquarters")
    var headquarters: String,
    @Column(name = "headquarters_postal_code")
    var headquartersPostalCode: String?,
    @Column(name = "sector")
    var sector: String?,
    @Column(name = "sector_code_wz")
    var sectorCodeWz: String?,
    @OneToMany(mappedBy = "company")
    var identifiers: MutableList<CompanyIdentifierEntity>,
    @Column(name = "parent_company_lei")
    var parentCompanyLei: String?,
    @OneToMany(mappedBy = "company")
    val dataRegisteredByDataland: MutableList<DataMetaInformationEntity>,
    @Column(name = "country_code")
    var countryCode: String,
    @Column(name = "is_teaser_company")
    var isTeaserCompany: Boolean,
    @Column(name = "website")
    var website: String?,
) : ApiModelConversion<StoredCompany> {
    @JsonValue
    override fun toApiModel(): StoredCompany {
        val identifierMap = createIdentifierMap()

        return StoredCompany(
            companyId = companyId,
            companyInformation =
                CompanyInformation(
                    companyName = companyName,
                    companyAlternativeNames = companyAlternativeNames,
                    companyLegalForm = companyLegalForm,
                    companyContactDetails = companyContactDetails,
                    headquarters = headquarters,
                    headquartersPostalCode = headquartersPostalCode,
                    sector = sector,
                    sectorCodeWz = sectorCodeWz,
                    identifiers = identifierMap,
                    countryCode = countryCode,
                    isTeaserCompany = isTeaserCompany,
                    website = website,
                    parentCompanyLei = parentCompanyLei,
                ),
            dataRegisteredByDataland = dataRegisteredByDataland.map { it.toApiModel() }.toMutableList(),
        )
    }

    private fun createIdentifierMap(): MutableMap<IdentifierType, MutableList<String>> {
        val identifierMap = mutableMapOf<IdentifierType, MutableList<String>>()
        for (identifierType in IdentifierType.entries) {
            identifierMap[identifierType] = mutableListOf()
        }

        for (identifier in identifiers) {
            val entry = identifierMap[identifier.identifierType]
            requireNotNull(entry)
            entry.add(identifier.identifierValue)
        }

        identifierMap.values.forEach { it.sort() }
        return identifierMap
    }
}
