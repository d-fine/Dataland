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
        val identifiers: MutableMap<String, List<String>> = HashMap()
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
            sectorCodeWz = sector,
            website = null,
            identifiers = identifiers,
            parentCompanyLei = null,
        )
    }

    /**
     * Transform the North Data company information to a PATCH object that can be used to update the information of the
     * company using the Dataland API
     */
    override fun toCompanyPatch(conflictingIdentifiers: Set<String?>?): CompanyInformationPatch? {
        var returnFullPatch = false

        if (conflictingIdentifiers != null) {
            val hasLei = conflictingIdentifiers.contains(IdentifierType.Lei.value)
            val hasRegisterId = conflictingIdentifiers.contains(IdentifierType.CompanyRegistrationNumber.value)
            val hasVatId = conflictingIdentifiers.contains(IdentifierType.VatNumber.value)
            if (!hasLei && (hasRegisterId || hasVatId)) returnFullPatch = true
        }

        val identifiers: MutableMap<String, List<String>> = HashMap()
        if (registerId != "") identifiers[IdentifierType.CompanyRegistrationNumber.value] = listOf(registerId)
        if (vatId != "") identifiers[IdentifierType.VatNumber.value] = listOf(vatId)

        if (!returnFullPatch) {
            return CompanyInformationPatch(
                sectorCodeWz = sector,
                identifiers = identifiers,
            )
        }

        return CompanyInformationPatch(
            companyName = companyName,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sectorCodeWz = sector,
            identifiers = identifiers,
        )
    }

    override fun getNameAndIdentifier(): String =
        "$companyName " +
            " (RegisterID: $registerId, VatId: $vatId and LEI: $lei)"
}
