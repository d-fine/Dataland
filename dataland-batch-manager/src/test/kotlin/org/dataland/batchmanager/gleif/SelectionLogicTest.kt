package org.dataland.batchmanager.gleif

import org.dataland.datalandbatchmanager.model.AlternativeEntityName
import org.dataland.datalandbatchmanager.model.CompanyNameSelectionStaticValues
import org.dataland.datalandbatchmanager.model.Entity
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.dataland.datalandbatchmanager.model.HeadquartersAddress
import org.dataland.datalandbatchmanager.model.LEIRecord
import org.dataland.datalandbatchmanager.model.LegalName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SelectionLogicTest {
    // Data class for defining test cases
    data class Case(
        val description: String,
        val legalName: LegalName,
        val transliteratedNames: List<AlternativeEntityName> = emptyList(),
        val otherEntityNames: List<AlternativeEntityName> = emptyList(),
        val expected: String,
    )

    companion object {
        private const val OTHER_NAME_1 = "Other Name 1"

        @JvmStatic
        fun provideCompanyCases() =
            listOf(
                *group1and2SimpleAndWhiteListedCases().toTypedArray(),
                *group3TransliterationsAndASCIIStringCases().toTypedArray(),
                *group4AutoASCIITransliterationCases().toTypedArray(),
                *group5aAlternativesInSingleLanguageCases().toTypedArray(),
                *group5bAlternativesInMultipleLanguagesCases().toTypedArray(),
            )

        // Group 1 and 2 combined: Simple and whitelisted cases
        private fun group1and2SimpleAndWhiteListedCases() =
            listOf(
                Case(
                    description = "Not whitelisted (ar), no transliterated or other names",
                    legalName = LegalName(lang = "ar", name = "سابك"),
                    expected = "سابك",
                ),
                Case(
                    description = "Not whitelisted (ar) with only one Trading Name in ar",
                    legalName = LegalName(lang = "ar", name = "سوكاما"),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type = CompanyNameSelectionStaticValues.AltenativeNameType.TRADING_OR_OPERATING_NAME.toString(),
                                name = "سوكاما SA",
                                lang = "ar",
                            ),
                        ),
                    expected = "سوكاما",
                ),
                Case(
                    description = "Whitelisted language with suffix (de-CH)",
                    legalName = LegalName(lang = "de-CH", name = "Zürich AG"),
                    expected = "Zürich AG",
                ),
            )

        // Group 3: Cases involving transliterations and ASCII strings
        private val case3aTransliterationsAndASCIIStrings =
            Case(
                description = "Not whitelisted (sv) with Preferred ASCII in en + Alternative in en-US",
                legalName = LegalName(lang = "sv", name = "Förening Aktiebolag"),
                transliteratedNames =
                    listOf(
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Forening Aktiebolag",
                            lang = "en",
                        ),
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Forening AKIEBOLAG",
                            lang = "en",
                        ),
                    ),
                otherEntityNames =
                    listOf(
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                    .toString(),
                            name = "Forening Ltd",
                            lang = "en-US",
                        ),
                    ),
                expected = "Forening Aktiebolag",
            )

        private val case3bTransliterationsAndASCIIStrings =
            Case(
                description = "Not whitelisted (sv) with Preferred ASCII in es + fr",
                legalName = LegalName(lang = "sv", name = "Målin Aktiebolag"),
                transliteratedNames =
                    listOf(
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Malin AB",
                            lang = "es",
                        ),
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Malin ab",
                            lang = "fr",
                        ),
                    ),
                expected = "Malin AB",
            )

        private fun group3TransliterationsAndASCIIStringCases() =
            listOf(
                case3aTransliterationsAndASCIIStrings,
                case3bTransliterationsAndASCIIStrings,
            )

        // Group 4: AUTO ASCII transliterations

        private val case4aAutoASCIITransliterations =
            Case(
                description = "Not whitelisted (da) with AUTO ASCII in en + fr",
                legalName = LegalName(lang = "da", name = "Høksve A/S"),
                transliteratedNames =
                    listOf(
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Hoksve AS",
                            lang = "en",
                        ),
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Hoksve as",
                            lang = "en",
                        ),
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "Hoksve",
                            lang = "fr",
                        ),
                    ),
                expected = "Hoksve AS",
            )

        private val case4bAutoASCIITransliterations =
            Case(
                description = "Not whitelisted (da) with AUTO ASCII in fr + pl",
                legalName = LegalName(lang = "da", name = "JVR A/S"),
                transliteratedNames =
                    listOf(
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "JVR a.s.",
                            lang = "fr",
                        ),
                        AlternativeEntityName(
                            type =
                                CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                    .toString(),
                            name = "JVR AS",
                            lang = "pl",
                        ),
                    ),
                expected = "JVR a.s.",
            )

        private fun group4AutoASCIITransliterationCases() =
            listOf(
                case4aAutoASCIITransliterations, case4bAutoASCIITransliterations,
            )

        // Group 5a: Cases with alternatives in a single language
        private fun group5aAlternativesInSingleLanguageCases() =
            listOf(
                Case(
                    description = "Not whitelisted (pl) with Alternative in en + AUTO ASCII",
                    legalName = LegalName(lang = "pl", name = "SPS S.A."),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.AUTO_ASCII_TRANSLITERATED_LEGAL_NAME
                                        .toString(),
                                name = "SPS SA",
                                lang = "en",
                            ),
                        ),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                        .toString(),
                                name = "SPS LLC",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                        .toString(),
                                name = "SPS Ltd",
                                lang = "en",
                            ),
                        ),
                    expected = "SPS LLC",
                ),
            )

        // Group 5b: Cases with alternatives in multiple languages
        private fun group5bAlternativesInMultipleLanguagesCases() =
            listOf(
                Case(
                    description = "Not whitelisted (pl) with Alternatives in en, de, es",
                    legalName = LegalName(lang = "pl", name = "Auron S.A."),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                        .toString(),
                                name = "Auron Ltd",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                        .toString(),
                                name = "Auron GmbH",
                                lang = "de",
                            ),
                            AlternativeEntityName(
                                type =
                                    CompanyNameSelectionStaticValues.AltenativeNameType.ALTERNATIVE_LANGUAGE_LEGAL_NAME
                                        .toString(),
                                name = "Auron SL",
                                lang = "es",
                            ),
                        ),
                    expected = "Auron Ltd",
                ),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCompanyCases")
    fun `company name resolution`(case: Case) {
        val entity =
            Entity(
                legalName = case.legalName,
                headquartersAddress = HeadquartersAddress(country = "XX", city = "City", postalCode = "0000"),
                transliteratedOtherEntityNames = case.transliteratedNames,
                otherEntityNames = case.otherEntityNames,
            )

        val gleifLeiRecord = LEIRecord(entity = entity, lei = "TEST123")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val result = combinedInfo.toCompanyPost().companyName
        assertEquals(case.expected, result, "Failed case: ${case.description}")
    }

    @Test
    fun `getGleifCompanyAlternativeNames includes all names except companyName and returns null if empty`() {
        // Arrange: Common setup data
        val legalName = LegalName(name = "Main Company Name", lang = "en") // This will also serve as `companyName`.
        val headquarters = HeadquartersAddress(country = "XX", city = "City", postalCode = "12345")

        val testCases =
            listOf(
                // Case 1: All names included except `companyName`
                Triple(
                    listOf("Transliterated Name 1", "Transliterated Name 2"),
                    listOf(OTHER_NAME_1, "Other Name 2"),
                    listOf("Transliterated Name 1", "Transliterated Name 2", OTHER_NAME_1, "Other Name 2"), // Expected
                ),
                // Case 2: `companyName` excluded
                Triple(
                    listOf("Main Company Name"), // Same as companyName, so it must be excluded
                    listOf(OTHER_NAME_1),
                    listOf(OTHER_NAME_1), // Expected
                ),
                // Case 3: No alternatives remain (should return null)
                Triple(
                    emptyList(), // No transliterated names
                    emptyList(), // No other names
                    null, // Expected
                ),
            )

        testCases.forEach { (transliterated, other, expected) ->
            val entity =
                Entity(
                    legalName = legalName,
                    headquartersAddress = headquarters,
                    transliteratedOtherEntityNames = transliterated.map { AlternativeEntityName(name = it) },
                    otherEntityNames = other.map { AlternativeEntityName(name = it) },
                )
            val gleifLeiRecord = LEIRecord(entity = entity, lei = "LEI12345")

            // Act: Instantiate the tested class and call the method
            val combinedInfo =
                GleifCompanyCombinedInformation(
                    gleifLeiRecord = gleifLeiRecord,
                    finalParentLei = null,
                )
            val result = combinedInfo.toCompanyPost().companyAlternativeNames

            assertEquals(expected, result)
        }
    }
}
