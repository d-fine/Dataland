package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch

/**
 * Data class containing the relevant information from the GLEIF csv files
 */
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
    /**
     * function to transform a company information object from GLEIF to the corresponding Dataland object.
     * @return the Dataland companyInformation object with the information of the corresponding GLEIF object
     */
    fun toCompanyPost(): CompanyInformation {
        return CompanyInformation(
            companyName = companyName,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sector = null,
            website = null,
            identifiers = mapOf(
                "Lei" to listOf(lei),
            ),
        )
    }

    /**
     * Transform the GLEIF company information to a PATCH object that can be used to update the information of the
     * company using the Dataland API
     */
    fun toCompanyPatch(): CompanyInformationPatch {
        return CompanyInformationPatch(
            companyName = companyName,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            identifiers = mapOf(
                "Lei" to listOf(lei),
            ),
        )
    }
}
