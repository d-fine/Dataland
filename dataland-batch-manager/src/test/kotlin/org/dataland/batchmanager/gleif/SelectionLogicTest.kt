package org.dataland.batchmanager.gleif

import org.dataland.datalandbatchmanager.model.AlternativeEntityName
import org.dataland.datalandbatchmanager.model.Entity
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.dataland.datalandbatchmanager.model.HeadquartersAddress
import org.dataland.datalandbatchmanager.model.LEIRecord
import org.dataland.datalandbatchmanager.model.LegalName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SelectionLogicTest {
    // Case 1: An entry that is whitelisted, e.g. "de" / "Müller GmbH"
    @Test
    fun `should retain LegalName if whitelisted`() {
        val legalName = LegalName(name = "Müller GmbH", lang = "de")
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress("DE", "Berlin", "10115"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "DE1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Müller GmbH", companyInformation.companyName)
    }

    // Case 2: Whitelisted language with a specification after the suffix, e.g. "de-CH" / "Zürich AG"
    @Test
    fun `should retain LegalName if whitelisted with suffix`() {
        val legalName = LegalName(name = "Zürich AG", lang = "de-CH")
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress("CH", "Zurich", "8001"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "CH0987654321")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Zürich AG", companyInformation.companyName)
    }

    // Case 3: An entry that is not whitelisted, e.g. "ar" / "سابك", but has no other LegalName at all
    @Test
    fun `should retain LegalName if not whitelisted and no alternatives`() {
        val legalName = LegalName(name = "سابك", lang = "ar")
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress("SA", "Riyadh", "11426"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "AR1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("سابك", companyInformation.companyName)
    }

    // Case 4: Entry in "sv" with Preferred ASCII in "en" and Alternative Legal Name in "en-US"
    @Test
    fun `should prioritize Preferred ASCII over Alternative Legal Name`() {
        val legalName = LegalName(name = "Förening Aktiebolag", lang = "sv")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Forening Aktiebolag", lang = "en"),
            )
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Forening Ltd", lang = "en-US"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "SE", city = "Stockholm", postalCode = "11120"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "SE1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Forening Aktiebolag", companyInformation.companyName)
    }

    // Case 5: Entry in "sv" with two Preferred ASCII Names in "en"
    @Test
    fun `should select first Preferred ASCII Name`() {
        val legalName = LegalName(name = "Svånsson HQ", lang = "sv")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Svansson HQ", lang = "en"),
                AlternativeEntityName(type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Svansson hq", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "SE", city = "Gothenburg", postalCode = "41120"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "SE0987654321")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Svansson HQ", companyInformation.companyName)
    }

    // Case 6: Entry in "sv" with two Preferred ASCII Names in "es" and "fr"
    @Test
    fun `should fallback to Preferred ASCII in non-en language`() {
        val legalName = LegalName(name = "Målin Aktiebolag", lang = "sv")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Malin AB", lang = "es"),
                AlternativeEntityName(type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Malin ab", lang = "fr"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "SE", city = "Malmo", postalCode = "21120"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "SE6789054321")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Malin AB", companyInformation.companyName)
    }

    // Case 7: LegalName in "ko" with a Trading or Operating Name in "en" and an Alternative Legal Name in "en"
    @Test
    fun `should select Trading or Operating Name over Alternative Legal Name`() {
        val legalName = LegalName(name = "롤라팔루자", lang = "ko")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "Lollapalooza", lang = "en"),
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Lollapalooza Ltd", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress("KR", "Seoul", "04524"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "KR0987123456")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Lollapalooza", companyInformation.companyName)
    }

// Case 8: LegalName in "ko" with two Trading or Operating Names in "en"
    @Test
    fun `should select the first Trading or Operating Name in en`() {
        val legalName = LegalName(name = "로부부", lang = "ko")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "Robubu Family", lang = "en"),
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "Robubu Company", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "KR", city = "Seoul", postalCode = "04524"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "KR1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Robubu Family", companyInformation.companyName)
    }

// Case 9: LegalName in "pl" with three Trading or Operating Names in "en", "en-US", and "es"
    @Test
    fun `should prioritize the first Trading or Operating Name in en`() {
        val legalName = LegalName(name = "해방하다", lang = "pl")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "LIBORT Tradings", lang = "en"),
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "LIBORT LTD", lang = "en-US"),
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "LIBORT SA", lang = "es"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "PL", city = "Warsaw", postalCode = "00-001"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "PL9876543210")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("LIBORT Tradings", companyInformation.companyName)
    }

// Case 10: LegalName in "pl" with an Alternative Legal Name in "en" and an AUTO ASCII Name in "en"
    @Test
    fun `should prioritize Alternative Legal Name over AUTO ASCII Name`() {
        val legalName = LegalName(name = "SPS S.A.", lang = "pl")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "SPS LLC", lang = "en"),
            )
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "SPS SA", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "PL", city = "Krakow", postalCode = "31-000"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "PL1239876543")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("SPS LLC", companyInformation.companyName)
    }

// Case 11: LegalName in "pl" with two Alternative Legal Names in "en"
    @Test
    fun `should select the first Alternative Legal Name in en`() {
        val legalName = LegalName(name = "Tietzy S.A.", lang = "pl")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Tietzy LLC", lang = "en"),
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Tietzy Ltd", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "PL", city = "Gdansk", postalCode = "80-001"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "PL6543210987")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Tietzy LLC", companyInformation.companyName)
    }

// Case 12: LegalName in "pl" with three Alternative Legal Names in multiple languages
    @Test
    fun `should prioritize Alternative Legal Name in en over other languages`() {
        val legalName = LegalName(name = "Auron S.A.", lang = "pl")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Auron Ltd", lang = "en"),
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Auron GmbH", lang = "de"),
                AlternativeEntityName(type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME", name = "Auron SL", lang = "es"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "PL", city = "Lodz", postalCode = "90-001"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "PL8901234567")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Auron Ltd", companyInformation.companyName)
    }

// Case 13: LegalName in "da" with two AUTO ASCII Names in "en"
    @Test
    fun `should select the first AUTO ASCII Name in en`() {
        val legalName = LegalName(name = "Høksve A/S", lang = "da")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Hoksve AS", lang = "en"),
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "Hoksve as", lang = "en"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "DK", city = "Copenhagen", postalCode = "1000"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "DK1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("Hoksve AS", companyInformation.companyName)
    }

// Case 14: LegalName in "da" with AUTO ASCII Name in "en" and "fr"
    @Test
    fun `should select AUTO ASCII Name in en over fr`() {
        val legalName = LegalName(name = "JVR A/S", lang = "da")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "JVR AS", lang = "en"),
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "JVR a.s.", lang = "fr"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "DK", city = "Aarhus", postalCode = "8000"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "DK5678901234")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()
        assertEquals("JVR AS", companyInformation.companyName)
    }

// Case 15: LegalName in "da" with AUTO ASCII Names in "fr" and "pl"
    @Test
    fun `should choose the first AUTO ASCII Name that is not en if no en exists`() {
        val legalName = LegalName(name = "JVR A/S", lang = "da")
        val transliteratedNames =
            listOf(
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "JVR a.s.", lang = "fr"),
                AlternativeEntityName(type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME", name = "JVR AS", lang = "pl"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "DK", city = "Odense", postalCode = "5000"),
                transliteratedOtherEntityNames = transliteratedNames,
                otherEntityNames = emptyList(),
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "DK8901234567")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()

        // Corrected assertion: The first non-"en" AUTO ASCII Name ("JVR a.s." in "fr") should be chosen
        assertEquals("JVR a.s.", companyInformation.companyName)
    }

    // Case 16: LegalName in "ar" with one non-English Trading or Operating Name in "ar"
    @Test
    fun `should fallback to LegalName if no valid other or transliterated name exists`() {
        val legalName = LegalName(name = "سوكاما", lang = "ar")
        val otherEntityNames =
            listOf(
                AlternativeEntityName(type = "TRADING_OR_OPERATING_NAME", name = "سوكاما SA", lang = "ar"),
            )
        val entity =
            Entity(
                legalName = legalName,
                headquartersAddress = HeadquartersAddress(country = "EG", city = "Cairo", postalCode = "11511"),
                transliteratedOtherEntityNames = emptyList(),
                otherEntityNames = otherEntityNames,
            )
        val gleifLeiRecord = LEIRecord(entity = entity, lei = "EG1234567890")
        val combinedInfo = GleifCompanyCombinedInformation(gleifLeiRecord)

        val companyInformation = combinedInfo.toCompanyPost()

        // Expectation: Fallback to LegalName because no valid transliterated or "en" operating name exists
        assertEquals("سوكاما", companyInformation.companyName)
    }
}
