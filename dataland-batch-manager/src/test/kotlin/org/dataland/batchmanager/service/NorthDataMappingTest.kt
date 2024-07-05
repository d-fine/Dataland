package org.dataland.batchmanager.service

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class NorthDataMappingTest {
    // TODO we could think about moving this test data into test .csv files. maybe is this more readable than
    //  defining it here
    private val testCompanyName = "test Gmbh"
    private val testHeadquarters = "Osnabrück"
    private val testSectorClassification = "73.11.0"
    private val expectedNorthDataCompanyInformation = NorthDataCompanyInformation(
        companyName = testCompanyName,
        headquarters = testHeadquarters,
        headquartersPostalCode = "49078",
        lei = "",
        countryCode = "DE",
        registerId = "Amtsgericht Osnabrück HRB 123456",
        street = "Test Weg 22",
        vatId = "",
        status = "active",
        sector = testSectorClassification,
    )

    private val expectedIdentifiers = mapOf(
        IdentifierType.CompanyRegistrationNumber.value to listOf("Amtsgericht Osnabrück HRB 123456"),
    )
    private val expectedCompanyInformation = CompanyInformation(
        companyName = testCompanyName,
        headquarters = testHeadquarters,
        headquartersPostalCode = "49078",
        sectorClassification = testSectorClassification,
        identifiers = expectedIdentifiers,
        countryCode = "DE",
    )

    private val expectedMinimalCompanyInformationPatch = CompanyInformationPatch(
        sectorClassification = testSectorClassification,
        identifiers = expectedIdentifiers,
    )

    private val expectedCompanyInformationPatch = CompanyInformationPatch(
        companyName = testCompanyName,
        headquarters = testHeadquarters,
        headquartersPostalCode = "49078",
        sectorClassification = testSectorClassification,
        identifiers = expectedIdentifiers,
        countryCode = "DE",
    )

    @Test
    fun `test north data data type`() {
        val zipFile = File("./build/resources/test/testHierarchicalFile.zip")

        val bufferedReader = GleifCsvParser().getCsvStreamFromNorthDataZipFile(zipFile)
        val iterable = GleifCsvParser().readNorthDataFromBufferedReader(bufferedReader)
        val onlyElement = iterable.iterator().next()
        assertEquals(expectedNorthDataCompanyInformation, onlyElement)
        assertEquals(expectedCompanyInformation, onlyElement.toCompanyPost())
        // Test all getters
        assertEquals(
            expectedNorthDataCompanyInformation,
            NorthDataCompanyInformation(
                headquarters = onlyElement.headquarters,
                headquartersPostalCode = onlyElement.headquartersPostalCode,
                countryCode = onlyElement.countryCode,
                companyName = onlyElement.companyName,
                registerId = onlyElement.registerId,
                street = onlyElement.street,
                vatId = onlyElement.vatId,
                status = onlyElement.status,
                sector = onlyElement.sector,
                lei = onlyElement.lei,
            ),
        )
        assertEquals(expectedMinimalCompanyInformationPatch, onlyElement.toCompanyPatch())
        assertEquals(
            expectedMinimalCompanyInformationPatch,
            onlyElement.toCompanyPatch(setOf("CompanyRegistrationNumber", "Lei")),
        )
        assertEquals(
            expectedMinimalCompanyInformationPatch,
            onlyElement.toCompanyPatch(setOf("Lei")),
        )
        assertEquals(
            expectedCompanyInformationPatch,
            onlyElement.toCompanyPatch(setOf("CompanyRegistrationNumber")),
        )
    }
}
