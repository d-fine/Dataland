package org.dataland.batchmanager.gleif

import org.dataland.datalandbatchmanager.model.AlternativeEntityName
import org.dataland.datalandbatchmanager.model.Entity
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.dataland.datalandbatchmanager.model.HeadquartersAddress
import org.dataland.datalandbatchmanager.model.LEIRecord
import org.dataland.datalandbatchmanager.model.LegalName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
        @JvmStatic
        fun provideCompanyCases() =
            listOf(
                *group1and2Cases().toTypedArray(),
                *group3Cases().toTypedArray(),
                *group4Cases().toTypedArray(),
                *group5aCases().toTypedArray(),
                *group5bCases().toTypedArray(),
            )

        // Group 1 and 2 combined: Simple and whitelisted cases
        private fun group1and2Cases() =
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
                                type = "TRADING_OR_OPERATING_NAME",
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
        private fun group3Cases() =
            listOf(
                Case(
                    description = "Not whitelisted (sv) with Preferred ASCII in en + Alternative in en-US",
                    legalName = LegalName(lang = "sv", name = "Förening Aktiebolag"),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Forening Aktiebolag",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Forening AKIEBOLAG",
                                lang = "en",
                            ),
                        ),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                name = "Forening Ltd",
                                lang = "en-US",
                            ),
                        ),
                    expected = "Forening Aktiebolag",
                ),
                Case(
                    description = "Not whitelisted (sv) with Preferred ASCII in es + fr",
                    legalName = LegalName(lang = "sv", name = "Målin Aktiebolag"),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Malin AB",
                                lang = "es",
                            ),
                            AlternativeEntityName(
                                type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Malin ab",
                                lang = "fr",
                            ),
                        ),
                    expected = "Malin AB",
                ),
            )

        // Group 4: AUTO ASCII transliterations
        private fun group4Cases() =
            listOf(
                Case(
                    description = "Not whitelisted (da) with AUTO ASCII in en + fr",
                    legalName = LegalName(lang = "da", name = "Høksve A/S"),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Hoksve AS",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Hoksve as",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "Hoksve",
                                lang = "fr",
                            ),
                        ),
                    expected = "Hoksve AS",
                ),
                Case(
                    description = "Not whitelisted (da) with AUTO ASCII in fr + pl",
                    legalName = LegalName(lang = "da", name = "JVR A/S"),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "JVR a.s.",
                                lang = "fr",
                            ),
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "JVR AS",
                                lang = "pl",
                            ),
                        ),
                    expected = "JVR a.s.",
                ),
            )

        // Group 5a: Cases with alternatives in a single language
        private fun group5aCases() =
            listOf(
                Case(
                    description = "Not whitelisted (pl) with Alternative in en + AUTO ASCII",
                    legalName = LegalName(lang = "pl", name = "SPS S.A."),
                    transliteratedNames =
                        listOf(
                            AlternativeEntityName(
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                name = "SPS SA",
                                lang = "en",
                            ),
                        ),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                name = "SPS LLC",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                name = "SPS Ltd",
                                lang = "en",
                            ),
                        ),
                    expected = "SPS LLC",
                ),
            )

        // Group 5b: Cases with alternatives in multiple languages
        private fun group5bCases() =
            listOf(
                Case(
                    description = "Not whitelisted (pl) with Alternatives in en, de, es",
                    legalName = LegalName(lang = "pl", name = "Auron S.A."),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                name = "Auron Ltd",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                name = "Auron GmbH",
                                lang = "de",
                            ),
                            AlternativeEntityName(
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
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
                    listOf("Other Name 1", "Other Name 2"),
                    listOf("Transliterated Name 1", "Transliterated Name 2", "Other Name 1", "Other Name 2"), // Expected
                ),
                // Case 2: `companyName` excluded
                Triple(
                    listOf("Main Company Name"), // Same as companyName, so it must be excluded
                    listOf("Other Name 1"),
                    listOf("Other Name 1"), // Expected
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

            // Assert: Validate the result
            if (expected == null) {
                assertNull(result)
            } else {
                assertEquals(expected, result)
            }
        }
    }
}
