package org.dataland.prepoulator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.dataland.datalandbackend.model.EuTaxonomyDetailsPerCashFlowType
import org.dataland.datalandbackend.model.enums.AttestationOptions
import org.dataland.datalandbackend.model.enums.IdentifierType
import org.dataland.datalandbackend.model.enums.StockIndex
import org.dataland.datalandbackend.model.enums.YesNo
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        "marketCap" to "Market Capitalization (EURmm)",
        "reportingDateOfMarketCap" to "Market Capitalization Date"
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
        return getValue(columnHeader, mapObject).trim().replace(".", "").replace(",", ".")
            .toBigDecimal().multiply("1000000".toBigDecimal())
    }

    private fun validateLine(csvData: Map<String, String>): Boolean {
        return !(
            getValue("IS/FS", csvData) in listOf("FS", notAvailableString) ||
                getValue(columnMapping["marketCap"]!!, csvData) == notAvailableString
            )
    }

    private fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return inputList.filter { validateLine(it) }.map {
            CompanyInformation(
                companyName = getValue(columnMapping["companyName"]!!, it),
                headquarters = getValue(columnMapping["headquarters"]!!, it),
                sector = getValue(columnMapping["sector"]!!, it),
                marketCap = getMarketCap(columnMapping["marketCap"]!!, it),
                reportingDateOfMarketCap = LocalDate.parse(getValue(columnMapping["reportingDateOfMarketCap"]!!, it),
                    DateTimeFormatter.ofPattern("MM.dd.yyyy")),
                identifiers = getIdentifiers(it),
                indices = getStockIndices(it)
            )
        }
    }

    /**
     * Method to write the transformed data into json
     */
    fun writeJson() {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(File("./CompanyInformation.json"), buildListOfCompanyInformation())
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(File("./CompanyAssociatedEuTaxonomyData.json"), buildListOfEuTaxonomyData())
    }

    private fun getReportingObligation(csvData: Map<String, String>): YesNo {
        return if (getValue("NFRD Pflicht" ,csvData) == "Ja") {
            YesNo.Yes
        } else {
            YesNo.No
        }
    }

    private fun getAttestation(csvData: Map<String, String>): AttestationOptions {
        val assurance = getValue("Assurance" ,csvData)
        return if (assurance == "reasonable") {
            AttestationOptions.ReasonableAssurance
        } else if (assurance == "limited") {
            AttestationOptions.LimitedAssurance
        } else {
            AttestationOptions.None
        }
    }

    private fun getCashFlowData(columnHeader: String, csvData: Map<String, String>): BigDecimal? {
        return getNumericValue(columnHeader, csvData)
    }

    private fun getNumericValue(columnHeader: String, csvData: Map<String, String>): BigDecimal? {
        return if (getValue(columnHeader, csvData).contains("%")) {
            getPercentageValue(columnHeader, csvData)
        } else {
            getAbsoluteValue(columnHeader, csvData)
        }
    }

    private fun getAbsoluteValue(columnHeader: String, csvData: Map<String, String>): BigDecimal? {
        // The numeric value conversion assumes the figures to be provided in millions using german notation
        // , hence, "," as decimal separator and "." to separate thousands
        return getValue(columnHeader, csvData).trim().replace(".", "").replace(",", ".")
            .toBigDecimalOrNull()?.multiply("1000000".toBigDecimal())
    }

    private fun getPercentageValue(columnHeader: String, csvData: Map<String, String>): BigDecimal? {
        return getValue(columnHeader, csvData).trim().replace(".", "").replace(",", ".").replace("%","").toBigDecimalOrNull()
    }

    private val cashFlowMapping = mapOf(
        "totalRevenue" to "Total Revenue in EURmio",
        "totalCapex" to "Total CapEx EURmio",
        "totalOpex" to "Total OpEx EURmio",
        "eligibleRevenue" to "Eligible Revenue",
        "eligibleCapex" to "Eligible CapEx",
        "eligibleOpex" to "Eligible OpEx",
        "alignedRevenue" to "Aligned Revenue",
        "alignedCapex" to "Aligned CapEx",
        "alignedOpex" to "Aligned OpEx"
    )

    private fun buildListOfEuTaxonomyData(): List<EuTaxonomyData> {
        val outputListOfEuTaxonomyData: MutableList<EuTaxonomyData> = mutableListOf()
        for (csvData in inputList) {
            if (!validateLine(csvData)) {
                continue
            }
            outputListOfEuTaxonomyData.add(
                EuTaxonomyData(
                    reportObligation = getReportingObligation(csvData),
                    attestation = getAttestation(csvData),
                    capex = EuTaxonomyDetailsPerCashFlowType(
                        total = getCashFlowData(cashFlowMapping["totalCapex"]!!, csvData),
                        aligned = getCashFlowData(cashFlowMapping["alignedCapex"]!!, csvData),
                        eligible = getCashFlowData(cashFlowMapping["eligibleCapex"]!!, csvData)
                    ),
                    opex = EuTaxonomyDetailsPerCashFlowType(
                        total = getCashFlowData(cashFlowMapping["totalOpex"]!!, csvData),
                        aligned = getCashFlowData(cashFlowMapping["alignedOpex"]!!, csvData),
                        eligible = getCashFlowData(cashFlowMapping["eligibleOpex"]!!, csvData)
                    ),
                    revenue = EuTaxonomyDetailsPerCashFlowType(
                        total = getCashFlowData(cashFlowMapping["totalRevenue"]!!, csvData),
                        aligned = getCashFlowData(cashFlowMapping["alignedRevenue"]!!, csvData),
                        eligible = getCashFlowData(cashFlowMapping["eligibleRevenue"]!!, csvData)
                    ),
                )
            )
        }
        return outputListOfEuTaxonomyData
    }
}
