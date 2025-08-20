package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.CompanyNameSelectionStaticValues.ENGLISH_LANGUAGE_STRING_GLEIF
import org.dataland.datalandbatchmanager.model.CompanyNameSelectionStaticValues.noCompanyNameReplacementLanguageWhiteList

/**
 * Combines GLEIF company information, including the LEI record and optional parent LEI,
 * and provides methods to generate standardized `CompanyInformation` objects.
 * Determines the display name following prioritization rules for name types and languages.
 */

data class GleifCompanyCombinedInformation(
    val gleifLeiRecord: LEIRecord,
    val finalParentLei: String? = null,
) : ExternalCompanyInformation {
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

    override fun toCompanyPatch(
        conflictingIdentifiers: Set<String?>?,
        existingAlternativeNames: List<String>?,
    ): CompanyInformationPatch? {
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

    internal val companyName = determineCompanyName()

    /**
     * Determines the company name to display based on priority rules.
     * Returns the legal name if whitelisted or no other names exist.
     * Otherwise, selects a name based on type and language.
     */

    private fun determineCompanyName(): String {
        val legalName = gleifLeiRecord.entity.legalName.name
        val transliterated = gleifLeiRecord.entity.transliteratedOtherEntityNames
        val otherNames = gleifLeiRecord.entity.otherEntityNames

        val isWhitelisted =
            noCompanyNameReplacementLanguageWhiteList.any {
                gleifLeiRecord.entity.legalName.lang
                    ?.startsWith(it) ?: false
            }

        return when {
            isWhitelisted -> legalName
            transliterated.isNullOrEmpty() && otherNames.isNullOrEmpty() -> legalName
            else ->
                selectNameByPriority(
                    legalName = legalName,
                    transliteratedNames = transliterated,
                    otherNames = otherNames,
                )
        }
    }

    private fun selectNameByPriority(
        legalName: String,
        transliteratedNames: List<AlternativeEntityName>?,
        otherNames: List<AlternativeEntityName>?,
    ): String {
        fun selectName(
            entityNames: List<AlternativeEntityName>?,
            type: String,
            allowNonEnglish: Boolean,
        ): String? =
            entityNames
                ?.filter { it.type == type }
                ?.partition { it.lang?.startsWith(ENGLISH_LANGUAGE_STRING_GLEIF) == true }
                ?.let { (englishNames, otherNames) ->
                    englishNames.firstOrNull()?.name ?: if (allowNonEnglish) otherNames.firstOrNull()?.name else null
                }

        return selectName(transliteratedNames, "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", true)
            ?: selectName(otherNames, "TRADING_OR_OPERATING_NAME", false)
            ?: selectName(otherNames, "ALTERNATIVE_LANGUAGE_LEGAL_NAME", false)
            ?: selectName(transliteratedNames, "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", true)
            ?: legalName
    }

    private fun getGleifCompanyAlternativeNames(): List<String>? {
        val transliteratedNames = gleifLeiRecord.entity.transliteratedOtherEntityNames?.map { it.name } ?: emptyList()
        val otherNames = gleifLeiRecord.entity.otherEntityNames?.map { it.name } ?: emptyList()

        return (transliteratedNames + otherNames).takeIf { it.isNotEmpty() }
    }
}
