package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType

/**
 * Data class combining the information found in the GLEIF LEI and RR files
 */
data class GleifCompanyCombinedInformation(
    val gleifCompanyInformation: GleifCompanyInformation,
    val finalParentLei: String? = null,
) : ExternalCompanyInformation {
    /**
     * function to transform a company information object from GLEIF to the corresponding Dataland object.
     * @return the Dataland companyInformation object with the information of the corresponding GLEIF object
     */
    override fun toCompanyPost(): CompanyInformation {
        return CompanyInformation(
            companyName = gleifCompanyInformation.companyName,
            companyContactDetails = null,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = gleifCompanyInformation.countryCode,
            headquarters = gleifCompanyInformation.headquarters,
            headquartersPostalCode = gleifCompanyInformation.headquartersPostalCode,
            sector = null,
            sectorCodeWz = null,
            website = null,
            identifiers = mapOf(
                "Lei" to listOf(gleifCompanyInformation.lei),
            ),
            parentCompanyLei = finalParentLei,
        )
    }

    /**
     * Transform the GLEIF company information to a PATCH object that can be used to update the information of the
     * company using the Dataland API
     */
    override fun toCompanyPatch(conflictingIdentifiers: Set<String?>?): CompanyInformationPatch? {
        // When updating from GLEIF data, the only conflicting identifier must always be the Lei
        if ((conflictingIdentifiers != null) &&
            !(conflictingIdentifiers.size == 1 && conflictingIdentifiers.contains(IdentifierType.Lei.value))
        ) { return null }

        return CompanyInformationPatch(
            companyName = gleifCompanyInformation.companyName,
            countryCode = gleifCompanyInformation.countryCode,
            headquarters = gleifCompanyInformation.headquarters,
            headquartersPostalCode = gleifCompanyInformation.headquartersPostalCode,
            identifiers = mapOf(
                "Lei" to listOf(gleifCompanyInformation.lei),
            ),
            parentCompanyLei = finalParentLei,
        )
    }

    override fun getNameAndIdentifier(): String {
        return "${gleifCompanyInformation.companyName} " +
            " (LEI: ${gleifCompanyInformation.lei})"
    }
}
