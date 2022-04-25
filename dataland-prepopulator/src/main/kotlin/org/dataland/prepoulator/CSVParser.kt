package org.dataland.prepoulator

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

/**
 * Class to transform company information and EU Taxonomy data delivered by csv into json format
 * @param filePath location of the csv file to be transformed
 */
class CSVParser(val filePath: String) {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
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

    private fun getValue(header: String, csvData: Map<String, String>): String {
        return csvData[header]!!.ifBlank {
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

    private fun getStockIndices(csvData: Map<String, String>): List<StockIndex> {
        return stockIndexMapping.keys.filter { csvData[stockIndexMapping[it]]!!.isNotBlank() }
    }

    private fun getIdentifiers(mapObject: Map<String, String>): List<CompanyIdentifier> {
        val identifiers = identifierMapping.keys.map {
            CompanyIdentifier(identifierValue = getValue(identifierMapping[it]!!, mapObject), identifierType = it)
        }
        return identifiers.filter { it.identifierValue != notAvailableString }
    }

    private fun getMarketCap(columnHeader: String, mapObject: Map<String, String>): BigDecimal {
        // The market cap conversion assumes the figures to be provided in millions using german notation
        // , hence, "," as decimal separator and "." to separate thousands
        return getValue(columnHeader, mapObject).replace(".", "").replace(",", ".")
            .toBigDecimal().multiply("1000000".toBigDecimal())
    }

    private fun validateLine(csvData: Map<String, String>): Boolean {
        /*var lineValid = true
        if (getValue("IS/FS", csvData) == "FS") {
            lineValid = false
        }
        if (getValue(columnMapping["marketCap"]!!, csvData) == notAvailableString) {
            lineValid = false
        }
        return lineValid*/
        return getValue(columnMapping["marketCap"]!!, csvData) != notAvailableString

    }

    private fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return  inputList.filter { validateLine(it) }.map {
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

    /**
     * Method to write the transformed data into json
     */
    fun writeJson() {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(File("./Output.json"), buildListOfCompanyInformation())
    }

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
    } */
}
