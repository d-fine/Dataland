package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation

@JsonIgnoreProperties(ignoreUnknown = true)
data class GleifCompanyInformation(
    @JsonProperty("Entity.LegalName")
    val companyName: String,

    @JsonProperty("Entity.HeadquartersAddress.City")
    val headquarters: String,

    @JsonProperty("Entity.HeadquartersAddress.PostalCode")
    val headquartersPostalCode: String,

    @JsonProperty("LEI")
    val lei: String,

    @JsonProperty("Entity.HeadquartersAddress.Country")
    val countryCode: String,
) {
    fun toCompanyInformation(): CompanyInformation {
        return CompanyInformation(
            companyName = companyName,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sector = "dummy",
            website = null,
            identifiers = listOf(
                CompanyIdentifier(identifierType = CompanyIdentifier.IdentifierType.lei, identifierValue = lei),
            ),
        )
    }
}
