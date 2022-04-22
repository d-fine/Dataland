package org.dataland.prepoulator

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.io.FileReader
import java.time.LocalDate

class CSVParser(val filePath: String) {

    private val notAvailableString = "n/a"

    private inline fun <reified T> readCsvFile(fileName: String): List<T> {
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

    private val inputList: List<Map<String, String>> = readCsvFile(filePath)

    private fun getValue(key: String, mapObject: Map<String, String>): String {
        return mapObject[key]!!.ifBlank {
            notAvailableString
        }
    }

    private val stockIndexMapping = mapOf(
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

    private val identifierMapping = mapOf(
        CompanyIdentifier.IdentifierType.permId to "PermId",
        CompanyIdentifier.IdentifierType.isin to "ISIN"
    )

    private fun getStockIndices(mapObject: Map<String, String>): List<CompanyInformation.Indices> {
        return stockIndexMapping.keys.filter { mapObject[stockIndexMapping[it]]!!.isNotBlank() }
    }

    private fun getIdentifiers(mapObject: Map<String, String>): List<CompanyIdentifier> {
        val identifiers = identifierMapping.keys.map {
            CompanyIdentifier(identifierValue = getValue(identifierMapping[it]!!, mapObject), identifierType = it)
        }
        return identifiers.filter { it.identifierValue != notAvailableString }
    }

    fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return inputList.map {
            CompanyInformation(
                companyName = getValue("Unternehmensname", it),
                headquarters = getValue("Hauptsitz", it),
                sector = getValue("Sektor", it),
                marketCap = getValue("Market Capitalization", it).toBigDecimal(),
                reportingDateOfMarketCap = LocalDate.parse(getValue("MarketDate", it)),
                identifiers = getIdentifiers(it),
                indices = getStockIndices(it)
            )
        }
    }

    /*
    fun buildJson() {
        use object mapper to write companyInformation.json from output of buildListOfCompanyInformation()
    }
*/

    /* ToDo as soon as all needed info in Excel file is available
    fun buildListOfEuTaxonomyData(): List<EuTaxonomyData> {
        val outputListOfEuTaxonomyData: MutableList<EuTaxonomyData> = mutableListOf()
        for (map in inputList) {
            outputListOfEuTaxonomyData.add(
                EuTaxonomyData(
                    reportingObligation = EuTaxonomyData.ReportingObligation.yes, //todo
                    attestation = EuTaxonomyData.Attestation.none, //todo
                    capex = EuTaxonomyDetailsPerCashFlowType(
                        total = BigDecimal(2),
                        aligned = BigDecimal(2),
                        eligible = BigDecimal(2)
                    ), //todo
                    opex = EuTaxonomyDetailsPerCashFlowType(
                        total = BigDecimal(2),
                        aligned = BigDecimal(2),
                        eligible = BigDecimal(2)
                    ), //todo
                    revenue = EuTaxonomyDetailsPerCashFlowType(
                        total = BigDecimal(2),
                        aligned = BigDecimal(2),
                        eligible = BigDecimal(2)
                    ), //todo
                )
            )
        }
        return outputListOfEuTaxonomyData
    }
    */

    /*
    Plan:

    counter=0
    listOfMaps
    currentDataMap = listOfMaps[counter]

    companyInformation = CompanyInformation(currentDataMap[Unternehmensname] = companyName ...)
    euTaxonomyData = EutaxonomyData( ...)

    to List<companyInformation>        List<exuTaxonomyData>

    List => jsons bauen => in reosurces verschieben
*/
}
