package org.dataland.datalandbatchmanager.gleif

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.io.FileReader
import java.nio.charset.StandardCharsets

class Mapping {

    private fun mapGleifEntryToCompanyInformation(gleifEntry: GleifCompanyInformation): CompanyInformation {
        return CompanyInformation(
            companyName = gleifEntry.companyName,
            companyAlternativeNames = null,
            companyLegalForm = null,
            countryCode = gleifEntry.countryCode,
            headquarters = gleifEntry.headquarters,
            headquartersPostalCode = gleifEntry.headquartersPostalCode,
            sector = "dummy",
            website = null,
            identifiers = listOf(CompanyIdentifier(identifierType = CompanyIdentifier.IdentifierType.lei, identifierValue = gleifEntry.lei)),
        )
    }

    private fun readGleifCsvFile(fileName: String): List<GleifCompanyInformation> {
        FileReader(fileName, StandardCharsets.UTF_8).use {
            val schema = CsvSchema.builder().setUseHeader(true).setReorderColumns(true)
                .addColumn("Entity.LegalName")
                .addColumn("Entity.HeadquartersAddress.City")
                .addColumn("Entity.HeadquartersAddress.PostalCode")
                .addColumn("LEI")
                .addColumn("Entity.HeadquartersAddress.Country")
                .build()
            return CsvMapper()
                .readerFor(GleifCompanyInformation::class.java)
                .with(schema)
                .readValues<GleifCompanyInformation>(it)
                .readAll()
                .toList()
        }
    }

    fun mappingTest() {
        // This is just a function for testing and should be removed later
        println("parsing file")
        val test = readGleifCsvFile("Insert your test file here")
        println("result:")
        println(test)
    }
}
