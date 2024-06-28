package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch

/**
 * Data class containing the relevant information from the Northdata csv files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NorthDataCompanyInformation(
    @JsonProperty("Entity.LegalName")
    val companyName: String,

    @JsonProperty("Entity.Ort")
    val headquarters: String,

    @JsonProperty("Entity.PLZ")
    val headquartersPostalCode: String,

    @JsonProperty("LEI")
    val lei: String,

    @JsonProperty("Entity.Land")
    val countryCode: String,

    @JsonProperty("Entity.RegisterId")
    val registerId: String,

    @JsonProperty("Entity.Straße")
    val street: String,

    @JsonProperty("Entity.USt.-Id")
    val vatId: String,

    @JsonProperty("Entity.Status")
    val status: String,
) {
    /**
     * function to transform a company information object from NorthData to the corresponding Dataland object.
     * @return the Dataland companyInformation object
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
                "Register-Id" to listOf(registerId),
                "Vat-Id" to listOf(vatId),
            ),
            parentCompanyLei = null,
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
                "Register-Id" to listOf(registerId),
                "Vat-Id" to listOf(vatId),
            ),
        )
    }
}
