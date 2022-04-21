package org.dataland.prepoulator

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.Identifier
import java.io.FileReader
import java.time.LocalDate
import java.util.*


class CSVParser(val filePath: String) {

    inline fun <reified T> readCsvFile(fileName: String): List<T> {
        val csvMapper = CsvMapper()
        FileReader(fileName).use { reader ->
            return csvMapper
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator(';'))
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
    }

    fun readCsv(): List<Map<String, String>> {
        return readCsvFile(filePath)
    }

    fun getValue(key: String, mapObject: Map<String, String>): String {
        return mapObject[key]!!.ifBlank {
            "n/a"
        }
    }

    fun getStockIndicies(mapObject: Map<String, String>): List<CompanyInformation.Indices> {
        val nameMapping = mapOf(
            CompanyInformation.Indices.primeStandards to "Prime Standard",
            CompanyInformation.Indices.generalStandards to "General Standard",
            CompanyInformation.Indices.scaleHdax to "Scale",
            CompanyInformation.Indices.cdax to "CDAX",
            CompanyInformation.Indices.gex to "GEX",
            CompanyInformation.Indices.dax to "DAX",
            CompanyInformation.Indices.mdax to "MDAX",
            CompanyInformation.Indices.sdax to "SDAX",
            CompanyInformation.Indices.tecDax to "TecDAX",
            CompanyInformation.Indices.dax50Esg to "DAX 50 ESG"
        )

        val foundIndices = mutableListOf<CompanyInformation.Indices>()
        for (index in nameMapping.keys) {
            if (mapObject[nameMapping[index]]!!.isNotBlank()) {
                foundIndices.add(index)
            }
        }
        return foundIndices
    }

    fun getIdentifiers(mapObject: Map<String, String>): List<CompanyIdentifier> {
        val nameMapping = mapOf(
            CompanyIdentifier.IdentifierType.permId to "PermId",
            CompanyIdentifier.IdentifierType.isin to "ISIN"
        )
        val foundIdentifiers = mutableListOf<CompanyIdentifier>()
        for (identifier in nameMapping.keys) {
            val identifierValue = getValue(nameMapping[identifier]!!, mapObject)
            if (identifierValue != "n/a") {
                foundIdentifiers.add(CompanyIdentifier(identifierValue = identifierValue, identifierType = identifier))
            }
        }

        return foundIdentifiers
    }

    fun buildListOfCompanyInformation() {
        val inputList = readCsv()
        for(map in inputList) {
            val companyInformation = CompanyInformation (
                companyName = getValue("Unternehmensname", map),
                headquarters = getValue("Hauptsitz", map),
                sector = getValue("Sektor", map),
                marketCap = getValue("Market Capitalization", map).toBigDecimal(),
                reportingDateOfMarketCap = LocalDate.parse(getValue("MarketDate", map)),
                identifiers = getIdentifiers(map),
                indices = getStockIndicies(map)
            )
        }
    }

    /*
    counter=0
    listOfMaps
    currentDataMap = listOfMaps[counter]

    companyInformation = CompanyInformation(currentDataMap[Unternehmensname] = companyName ...)
    euTaxonomyData = EutaxonomyData( ...)

    to List<companyInformation>        List<exuTaxonomyData>

    List => jsons bauen => in reosurces verschieben
*/
}