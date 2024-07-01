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
    @JsonProperty("Name")
    val companyName: String,

    @JsonProperty("Ort")
    val headquarters: String,

    @JsonProperty("PLZ")
    val headquartersPostalCode: String,

    @JsonProperty("LEI")
    val lei: String,

    @JsonProperty("Land")
    val countryCode: String,

    @JsonProperty("Register ID")
    val registerId: String,

    @JsonProperty("Straße")
    val street: String?,

    @JsonProperty("USt.-Id.")
    val vatId: String,

    @JsonProperty("Status")
    val status: String,

    @JsonProperty("Branchencode")
    val sector: String?,

) : ExternalCompanyInformation {
    /**
     * function to transform a company information object from NorthData to the corresponding Dataland object.
     * @return the Dataland companyInformation object
     */
    override fun toCompanyPost(): CompanyInformation {
        var identifiers: MutableMap<String, List<String>> = HashMap()
        if (lei != "") identifiers[IdentifierType.Lei.value] = listOf(lei)
        if (registerId != "") identifiers[IdentifierType.CompanyRegistrationNumber.value] = listOf(registerId)
        if (vatId != "") identifiers[IdentifierType.VatNumber.value] = listOf(vatId)

        return CompanyInformation(
            companyName = companyName,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sector = null,
            website = null,
            identifiers = identifiers,
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
