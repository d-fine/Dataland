package org.dataland.prepoulator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.IdentifierType
import org.dataland.datalandbackend.model.enums.StockIndex
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.time.LocalDate

class CSVParser(val filePath: String) {

    //private val objectMapper = ObjectMapper().setDefaultPrettyPrinter().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
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
        StockIndex.PrimeStandard to "Prime Standard",
        StockIndex.GeneralStandard to "General Standard",
        StockIndex.ScaleHdax to "Scale",
        StockIndex.Cdax to "CDAX",
        StockIndex.Gex to "GEX",
        StockIndex.Dax to "DAX",
        StockIndex.Mdax to "MDAX",
        StockIndex.Sdax to "SDAX",
        StockIndex.TecDax to "TecDAX",
        StockIndex.Dax50Esg to "DAX 50 ESG"
    )

    private val identifierMapping = mapOf(
        IdentifierType.PermId to "PermID",
        IdentifierType.Isin to "ISIN"
    )

    private val columnMapping = mapOf(
        "companyName" to "Company name",
        "headquarters" to "Headquarter",
        "sector" to "Sektor",
        "marketCap" to "Market Capitalization",
        "reportingDateOfMarketCap" to "2021-12-31"
    )

    private fun getStockIndices(mapObject: Map<String, String>): List<StockIndex> {
        return stockIndexMapping.keys.filter { mapObject[stockIndexMapping[it]]!!.isNotBlank() }
    }

    private fun getIdentifiers(mapObject: Map<String, String>): List<CompanyIdentifier> {
        val identifiers = identifierMapping.keys.map {
            CompanyIdentifier(identifierValue = getValue(identifierMapping[it]!!, mapObject), identifierType = it)
        }
        return identifiers.filter { it.identifierValue != notAvailableString }
    }

    private fun getMarketCap(columnHeader: String, mapObject: Map<String, String>): BigDecimal {
        println(getValue(columnHeader, mapObject))
        return getValue(columnHeader, mapObject).replace(".","").replace(",",".")
            .toBigDecimal().multiply("1000000".toBigDecimal())
    }

    fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return inputList.map {
            CompanyInformation(
                companyName = getValue(columnMapping["companyName"]!!, it),
                headquarters = getValue(columnMapping["headquarters"]!!, it),
                sector = getValue(columnMapping["sector"]!!, it),
                marketCap = getMarketCap(columnMapping["marketCap"]!!, it),
                reportingDateOfMarketCap = LocalDate.parse(columnMapping["reportingDateOfMarketCap"]),
                identifiers = getIdentifiers(it),
                indices = getStockIndices(it)
            )
        }
    }


    fun buildJson() {
        val allCompanies = buildListOfCompanyInformation()
        println(allCompanies)
        println(objectMapper.writeValueAsString(allCompanies))
        //private val outFile = ClassPathResource("/Output.json").file
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(File("./Output.json"), allCompanies)
        //objectMapper.writeValue(outFile, allCompanies)
        //use object mapper to write companyInformation.json from output of buildListOfCompanyInformation()
    }

    fun readJson() {
        //val companyType: ParameterizedType = Types
        //    .newParameterizedType(List::class.java, CompanyInformation::class.java)
        val test: List<CompanyInformation> = objectMapper.readValue(File("./Output.json"), object : TypeReference<List<CompanyInformation>>() {})
        //val test = objectMapper.readValue(File("./Output.json"))

        println(test)
    }

    //data class CompanyInformationList: List<CompanyInformation>()



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
