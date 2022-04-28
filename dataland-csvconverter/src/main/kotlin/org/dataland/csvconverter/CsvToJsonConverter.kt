package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.dataland.datalandbackend.model.CompanyAssociatedData
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
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Class to transform company information and EU Taxonomy data delivered by csv into json format
 * @param filePath location of the csv file to be transformed
 */
class CsvToJsonConverter(private val filePath: String) {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    private val notAvailableString = "n/a"
    private var euroUnitConverter = "1000000"
    private val rawCsvData: List<Map<String, String>> = readCsvFile(filePath)

    private val columnMapping = mapOf(
        "companyName" to "Company name",
        "headquarters" to "Headquarter",
        "sector" to "Sektor",
        "marketCap" to "Market Capitalization (EURmm)",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
        "totalRevenue" to "Total Revenue in EURmio",
        "totalCapex" to "Total CapEx EURmio",
        "totalOpex" to "Total OpEx EURmio",
        "eligibleRevenue" to "Eligible Revenue",
        "eligibleCapex" to "Eligible CapEx",
        "eligibleOpex" to "Eligible OpEx",
        "alignedRevenue" to "Aligned Revenue",
        "alignedCapex" to "Aligned CapEx",
        "alignedOpex" to "Aligned OpEx",
        "companyType" to "IS/FS",
        "reportObligation" to "NFRD Pflicht",
        "attestation" to "Assurance"
    )

    private val stockIndexMapping = mapOf(
        StockIndex.PrimeStandard to "Prime Standard",
        StockIndex.GeneralStandard to "General Standard",
        StockIndex.Hdax to "HDAX",
        StockIndex.Scale to "Scale",
        StockIndex.Cdax to "CDAX",
        StockIndex.Gex to "GEX",
        StockIndex.Dax to "DAX",
        StockIndex.Mdax to "MDAX",
        StockIndex.Sdax to "SDAX",
        StockIndex.TecDax to "TecDAX",
        StockIndex.Dax50Esg to "DAX 50 ESG"
    )

    private val identifierMapping = mapOf(
        IdentifierType.Isin to "ISIN",
        IdentifierType.Lei to "LEI",
        IdentifierType.PermId to "PermID"
    )

    private inline fun <reified T> readCsvFile(fileName: String): List<T> {
        FileReader(fileName, StandardCharsets.UTF_8).use { reader ->
            return CsvMapper()
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator(';'))
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
    }

    /**
     * Method to define the conversion factor for absolute euro amounts
     * The default is 1000000 (meaning a number of 1 in the csv is interpreted as 1 million)
     */
    fun setEuroUnitConverter(conversionFactor: String): CsvToJsonConverter {
        euroUnitConverter = conversionFactor
        return this
    }

    /**
     * Method to get a list of CompanyInformation objects generated from the csv file
     */
    fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return rawCsvData.filter { validateLine(it) }.map {
            CompanyInformation(
                companyName = getValue(columnMapping["companyName"]!!, it),
                headquarters = getValue(columnMapping["headquarters"]!!, it),
                sector = getValue(columnMapping["sector"]!!, it),
                marketCap = getMarketCap(columnMapping["marketCap"]!!, it),
                reportingDateOfMarketCap = LocalDate.parse(
                    getValue(columnMapping["reportingDateOfMarketCap"]!!, it),
                    DateTimeFormatter.ofPattern("d.M.yyyy")
                ),
                identifiers = getIdentifiers(it),
                indices = getStockIndices(it)
            )
        }
    }

    private fun validateLine(csvLineData: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return !(
            getValue(columnMapping["companyType"]!!, csvLineData) in listOf("FS", notAvailableString) ||
                getValue(columnMapping["marketCap"]!!, csvLineData) == notAvailableString
            )
    }

    private fun getValue(columnName: String, csvData: Map<String, String>): String {
        return csvData[columnName]!!.trim().ifBlank {
            notAvailableString
        }
    }

    private fun getMarketCap(columnHeader: String, csvLineData: Map<String, String>): BigDecimal {
        return getScaledValue(columnHeader, csvLineData, euroUnitConverter)!!
    }

    private fun getScaledValue(columnHeader: String, csvData: Map<String, String>, scaleFactor: String): BigDecimal? {
        // The numeric value conversion assumes "," as decimal separator and "." to separate thousands
        return getValue(columnHeader, csvData).replace("[^,\\d]".toRegex(), "").replace(",", ".")
            .toBigDecimalOrNull()?.multiply(scaleFactor.toBigDecimal())
    }

    private fun getIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return identifierMapping.keys.map {
            CompanyIdentifier(identifierValue = getValue(identifierMapping[it]!!, csvLineData), identifierType = it)
        }.filter { it.identifierValue != notAvailableString }
    }

    private fun getStockIndices(csvLineData: Map<String, String>): Set<StockIndex> {
        return stockIndexMapping.keys.filter { csvLineData[stockIndexMapping[it]]!!.isNotBlank() }.toSet()
    }

    /**
     * Method to get a list of CompanyAssociatedEuTaxonomyData objects generated from the csv file
     */
    fun buildListOfEuTaxonomyData(): List<CompanyAssociatedData<EuTaxonomyData>> {
        return rawCsvData.filter { validateLine(it) }.withIndex().map { (index, csvLineData) ->
            CompanyAssociatedData(
                companyId = "${index + 1}",
                EuTaxonomyData(
                    reportObligation = getReportingObligation(csvLineData),
                    attestation = getAttestation(csvLineData),
                    capex = buildEuTaxonomyDetailsPerCashFlowType("Capex", csvLineData),
                    opex = buildEuTaxonomyDetailsPerCashFlowType("Opex", csvLineData),
                    revenue = buildEuTaxonomyDetailsPerCashFlowType("Revenue", csvLineData)
                )
            )
        }
    }

    private fun getReportingObligation(csvLineData: Map<String, String>): YesNo {
        return if (getValue(columnMapping["reportObligation"]!!, csvLineData) == "Ja") {
            YesNo.Yes
        } else {
            YesNo.No
        }
    }

    private fun getAttestation(csvLineData: Map<String, String>): AttestationOptions {
        return when (getValue(columnMapping["attestation"]!!, csvLineData)) {
            "reasonable" -> AttestationOptions.ReasonableAssurance
            "limited" -> AttestationOptions.LimitedAssurance
            else -> AttestationOptions.None
        }
    }

    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            total = getNumericValue(columnMapping["total$type"]!!, csvLineData),
            aligned = getNumericValue(columnMapping["aligned$type"]!!, csvLineData),
            eligible = getNumericValue(columnMapping["eligible$type"]!!, csvLineData)
        )
    }

    private fun getNumericValue(columnHeader: String, csvLineData: Map<String, String>): BigDecimal? {
        return if (getValue(columnHeader, csvLineData).contains("%")) {
            getScaledValue(columnHeader, csvLineData, "0.01")
        } else {
            getScaledValue(columnHeader, csvLineData, euroUnitConverter)
        }
    }

    /**
     * Method to write the transformed data into json
     */
    fun writeJson() {
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(File("./CompanyInformation.json"), buildListOfCompanyInformation())
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(File("./CompanyAssociatedEuTaxonomyData.json"), buildListOfEuTaxonomyData())
    }

    companion object {
        /**
         * The corresponding main class to run the CSV converter. Execute by running:
         * "./gradlew :dataland-csvconverter:run --args="<FileLocation>"
         * where <FileLocation> is the location of the CSV file to be converted
         */
        @JvmStatic
        fun main(args: Array<String>) {
            CsvToJsonConverter(File(args.first()).path).writeJson()
        }
    }
}
