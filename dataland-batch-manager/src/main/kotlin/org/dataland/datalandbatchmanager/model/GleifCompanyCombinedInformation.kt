package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.CompanyNameSelectionStaticValues.ENGLISH_LANGUAGE_STRING_GLEIF
import org.dataland.datalandbatchmanager.model.CompanyNameSelectionStaticValues.noCompanyNameReplacementLanguageWhiteList

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
            companyName = companyName,
            companyContactDetails = null,
            companyAlternativeNames = getGleifCompanyAlternativeNames(),
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
    override fun toCompanyPatch(
        conflictingIdentifiers: Set<String?>?,
        existingAlternativeNames: List<String>?,
    ): CompanyInformationPatch? {
        // When updating from GLEIF data, the only conflicting identifier must always be the Lei
        if ((conflictingIdentifiers != null) &&
            !(conflictingIdentifiers.size == 1 && conflictingIdentifiers.contains(IdentifierType.Lei.value))
        ) {
            return null
        }
        val gleifCompanyAlternativeNames = getGleifCompanyAlternativeNames()
        return CompanyInformationPatch(
            companyName = companyName,
            countryCode = gleifLeiRecord.entity.headquartersAddress.country,
            headquarters = gleifLeiRecord.entity.headquartersAddress.city,
            headquartersPostalCode = gleifLeiRecord.entity.headquartersAddress.postalCode,
            identifiers =
                mapOf(
                    "Lei" to listOf(gleifLeiRecord.lei),
                ),
            parentCompanyLei = finalParentLei,
            companyAlternativeNames =
                (existingAlternativeNames ?: emptyList()) + (
                    gleifCompanyAlternativeNames
                        ?: emptyList()
                ),
        )
    }

    override fun getNameAndIdentifier(): String =
        "${gleifLeiRecord.entity.legalName} " +
            " (LEI: ${gleifLeiRecord.lei})"

    private val companyName = determineCompanyName()

    private fun determineCompanyName(): String {
        val preferredASCIITransliteratedLegalName =
            gleifLeiRecord.entity.transliteratedOtherEntityNames
                ?.firstOrNull { it.type == "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME" }
                ?.name
        val alternativeLanguageLegalName =
            findShortestEnglishName(fieldName = "ALTERNATIVE_LANGUAGE_LEGAL_NAME")

        val tradingOrOperatingName =
            findShortestEnglishName(fieldName = "TRADING_OR_OPERATING_NAME")

        val autoASCIITransliteratedLegalName =
            gleifLeiRecord.entity.transliteratedOtherEntityNames
                ?.firstOrNull { it.type == "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME" }
                ?.name

        return if (noCompanyNameReplacementLanguageWhiteList.any {
                gleifLeiRecord.entity.legalName.lang
                    ?.startsWith(it)
                    ?: false
            }
        ) {
            gleifLeiRecord.entity.legalName.name
        } else {
            preferredASCIITransliteratedLegalName
                ?: (
                    alternativeLanguageLegalName
                        ?: (
                            tradingOrOperatingName
                                ?: (autoASCIITransliteratedLegalName ?: gleifLeiRecord.entity.legalName.name)
                        )
                )
        }
    }

    private fun findShortestEnglishName(fieldName: String): String? =
        gleifLeiRecord.entity.otherEntityNames
            ?.filter {
                it.type == fieldName &&
                    it.lang?.startsWith(
                        ENGLISH_LANGUAGE_STRING_GLEIF,
                    ) == true
            }?.minByOrNull { it.name.length }
            ?.name

    private fun getGleifCompanyAlternativeNames(): List<String>? {
        val alternativeGleifNames =
            gleifLeiRecord.entity.otherEntityNames
                ?.map { it.name }
                ?.plus(
                    (
                        gleifLeiRecord.entity.transliteratedOtherEntityNames?.map { it.name }
                            ?: emptyList()
                    ),
                )
        return if (companyName == gleifLeiRecord.entity.legalName.name) {
            alternativeGleifNames
        } else {
            alternativeGleifNames?.filterNot { it == companyName }?.plus(gleifLeiRecord.entity.legalName.name)
        }
    }
}
