package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType

/**
 * Data class combining the information found in the GLEIF LEI and RR files
 */
data class GleifCompanyCombinedInformation(
    val gleifLeiRecord: LEIRecord,
    val finalParentLei: String? = null,
) : ExternalCompanyInformation {
    /**
     * function to transform a company information object from GLEIF to the corresponding Dataland object.
     * @return the Dataland companyInformation object with the information of the corresponding GLEIF object
     */
    override fun toCompanyPost(): CompanyInformation =
        CompanyInformation(
            companyName = gleifLeiRecord.entity.legalName,
            companyContactDetails = null,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = gleifLeiRecord.entity.headquartersAddress.country,
            headquarters = gleifLeiRecord.entity.headquartersAddress.city,
            headquartersPostalCode = gleifLeiRecord.entity.headquartersAddress.postalCode,
            sector = null,
            sectorCodeWz = null,
            website = null,
            identifiers =
                mapOf(
                    "Lei" to listOf(gleifLeiRecord.lei),
                ),
            parentCompanyLei = finalParentLei,
        )

    /**
     * Transform the GLEIF company information to a PATCH object that can be used to update the information of the
     * company using the Dataland API
     */
    override fun toCompanyPatch(conflictingIdentifiers: Set<String?>?): CompanyInformationPatch? {
        // When updating from GLEIF data, the only conflicting identifier must always be the Lei
        if ((conflictingIdentifiers != null) &&
            !(conflictingIdentifiers.size == 1 && conflictingIdentifiers.contains(IdentifierType.Lei.value))
        ) {
            return null
        }

        return CompanyInformationPatch(
            companyName = gleifLeiRecord.entity.legalName,
            countryCode = gleifLeiRecord.entity.headquartersAddress.country,
            headquarters = gleifLeiRecord.entity.headquartersAddress.city,
            headquartersPostalCode = gleifLeiRecord.entity.headquartersAddress.postalCode,
            identifiers =
                mapOf(
                    "Lei" to listOf(gleifLeiRecord.lei),
                ),
            parentCompanyLei = finalParentLei,
        )
    }

    override fun getNameAndIdentifier(): String =
        "${gleifLeiRecord.entity.legalName} " +
            " (LEI: ${gleifLeiRecord.lei})"
}
