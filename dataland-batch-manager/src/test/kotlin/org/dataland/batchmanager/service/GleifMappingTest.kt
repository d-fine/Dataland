package org.dataland.batchmanager.service

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.AlternativeEntityName
import org.dataland.datalandbatchmanager.model.Entity
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.dataland.datalandbatchmanager.model.HeadquartersAddress
import org.dataland.datalandbatchmanager.model.LEIRecord
import org.dataland.datalandbatchmanager.service.CompanyInformationParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ComponentScan
import java.io.BufferedReader
import java.io.FileReader

@ComponentScan(basePackages = ["org.dataland"])
class GleifMappingTest {
    private val expectedGleifRecord =
        LEIRecord(
            lei = "DummyLei",
            entity =
                Entity(
                    legalName = "CompanyName",
                    headquartersAddress =
                        HeadquartersAddress(
                            city = "CompanyCity",
                            postalCode = "CompanyPostalCode",
                            country = "CompanyCountry",
                        ),
                    otherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                name = "Dummy english alternative language legal name 1",
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                name = "Dummy english alternative language legal name 2",
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                name = "Dummy non-english alternative language legal name",
                                type = "ALTERNATIVE_LANGUAGE_LEGAL_NAME",
                                lang = "zh",
                            ),
                            AlternativeEntityName(
                                name = "Dummy previous legal name",
                                type = "PREVIOUS_LEGAL_NAME",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                name = "Dummy trading or operating name",
                                type = "TRADING_OR_OPERATING_NAME",
                                lang = "en",
                            ),
                        ),
                    transliteratedOtherEntityNames =
                        listOf(
                            AlternativeEntityName(
                                name = "Dummy preferred ascii transliterated legal name",
                                type = "PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME",
                                lang = "en",
                            ),
                            AlternativeEntityName(
                                name = "Dummy auto ascii transliterated legal name",
                                type = "AUTO_ASCII_TRANSLITERATED_LEGAL_NAME",
                                lang = "en",
                            ),
                        ),
                ),
        )

    private val expectedCompanyInformation =
        CompanyInformation(
            companyName = expectedGleifRecord.entity.legalName,
            companyContactDetails = null,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = expectedGleifRecord.entity.headquartersAddress.country,
            headquarters = expectedGleifRecord.entity.headquartersAddress.city,
            headquartersPostalCode = expectedGleifRecord.entity.headquartersAddress.postalCode,
            sector = null,
            website = null,
            identifiers =
                mapOf(
                    IdentifierType.Lei.value to listOf(expectedGleifRecord.lei),
                ),
        )

    @Test
    fun `check that parsing the test file results in the expected company information objects`() {
        val input = BufferedReader(FileReader("./build/resources/test/GleifTestData.xml"))
        val gleifRecord = CompanyInformationParser().readGleifCompanyDataFromBufferedReader(input).leiRecords.first()

        println(gleifRecord.entity.legalName)
        val companyInformation = GleifCompanyCombinedInformation(gleifRecord).toCompanyPost()
        println(companyInformation)
        Assertions.assertEquals(
            expectedGleifRecord,
            gleifRecord,
            "The gleifCompanyInformation created based on the test csv file is not as expected.",
        )
        Assertions.assertEquals(
            expectedCompanyInformation,
            companyInformation,
            "The companyInformation created based on the test csv file is not as expected.",
        )
    }
}
