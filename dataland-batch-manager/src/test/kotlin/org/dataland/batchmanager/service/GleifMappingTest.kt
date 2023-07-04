package org.dataland.batchmanager.service

import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ComponentScan
import java.io.BufferedReader
import java.io.FileReader

@ComponentScan(basePackages = ["org.dataland"])
class GleifMappingTest {
    private val expectedGleifCompanyInformation = GleifCompanyInformation(
        companyName = "CompanyName",
        countryCode = "CompanyCountry",
        headquarters = "CompanyCity",
        headquartersPostalCode = "CompanyPostalCode",
        lei = "DummyLei",
    )

    private val expectedCompanyInformation = CompanyInformation(
        companyName = expectedGleifCompanyInformation.companyName,
        companyAlternativeNames = null,
        companyLegalForm = null,
        countryCode = expectedGleifCompanyInformation.countryCode,
        headquarters = expectedGleifCompanyInformation.headquarters,
        headquartersPostalCode = expectedGleifCompanyInformation.headquartersPostalCode,
        sector = null,
        website = null,
        identifiers = listOf(
            CompanyIdentifier(
                identifierType = CompanyIdentifier.IdentifierType.lei,
                identifierValue = expectedGleifCompanyInformation.lei,
            ),
        ),
    )

    @Test
    fun `check that parsing the test file results in the expected company information objects`() {
        val input = BufferedReader(FileReader("./build/resources/test/GleifTestData.csv"))
        val gleifCompanyInformation = GleifCsvParser().readGleifDataFromBufferedReader(input).next()
        val companyInformation = gleifCompanyInformation.toCompanyInformation()
        Assertions.assertEquals(
            expectedGleifCompanyInformation,
            gleifCompanyInformation,
            "The gleifCompanyInformation created based on the test csv file is not as expected.",
        )
        Assertions.assertEquals(
            expectedCompanyInformation,
            companyInformation,
            "The companyInformation created based on the test csv file is not as expected.",
        )
    }
}
