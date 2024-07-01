package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType

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
) : ExternalCompanyInformation {
    /**
     * function to transform a company information object from NorthData to the corresponding Dataland object.
     * @return the Dataland companyInformation object
     */
    override fun toCompanyPost(): CompanyInformation {
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
                IdentifierType.Lei.value to listOf(lei),
                IdentifierType.CompanyRegistrationNumber.value to listOf(registerId),
                IdentifierType.VatNumber.value to listOf(vatId),
            ),
            parentCompanyLei = null,
        )
    }

    /**
     * Transform the North Data company information to a PATCH object that can be used to update the information of the
     * company using the Dataland API
     */
    override fun toCompanyPatch(): CompanyInformationPatch {
        return CompanyInformationPatch(
            identifiers = mapOf(
                IdentifierType.CompanyRegistrationNumber.value to listOf(registerId),
                IdentifierType.VatNumber.value to listOf(vatId),
            ),
        )
    }

    override fun getNameAndIdentifier(): String {
        return "$companyName " +
                " (RegisterID: $registerId) and VatId: $vatId)"
    }
}
