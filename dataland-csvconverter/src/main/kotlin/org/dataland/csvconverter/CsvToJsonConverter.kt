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
 */
class CsvToJsonConverter {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    private val notAvailableString = "n/a"
    private var euroUnitConverter = "1"
    private var rawCsvData: List<Map<String, String>> = listOf()

    private val columnMapping = mapOf(
        "companyName" to "Company name",
        "headquarters" to "Headquarter",
        "sector" to "Sector",
        "marketCap" to "Market Capitalization EUR",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
        "totalRevenue" to "Total Revenue EUR",
        "totalCapex" to "Total CapEx EUR",
        "totalOpex" to "Total OpEx EUR",
        "eligibleRevenue" to "Eligible Revenue",
        "eligibleCapex" to "Eligible CapEx",
        "eligibleOpex" to "Eligible OpEx",
        "alignedRevenue" to "Aligned Revenue",
        "alignedCapex" to "Aligned CapEx",
        "alignedOpex" to "Aligned OpEx",
        "companyType" to "IS/FS",
        "reportObligation" to "NFRD mandatory",
        "attestation" to "Assurance",
        IdentifierType.Isin.name to "ISIN",
        IdentifierType.Lei.name to "LEI",
        IdentifierType.PermId.name to "PermID",
        StockIndex.PrimeStandard.name to "Prime Standard",
        StockIndex.GeneralStandard.name to "General Standard",
        StockIndex.Hdax.name to "HDAX",
        StockIndex.Scale.name to "Scale",
        StockIndex.Cdax.name to "CDAX",
        StockIndex.Gex.name to "GEX",
        StockIndex.Dax.name to "DAX",
        StockIndex.Mdax.name to "MDAX",
        StockIndex.Sdax.name to "SDAX",
        StockIndex.TecDax.name to "TecDAX",
        StockIndex.Dax50Esg.name to "DAX 50 ESG"
    )

    private inline fun <reified T> readCsvFile(fileName: String): List<T> {
        FileReader(fileName, StandardCharsets.UTF_8).use {
            return CsvMapper()
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator(';'))
                .readValues<T>(it)
                .readAll()
                .toList()
        }
    }

    /**
     * Method to read a given csv file
     */
    fun parseCsvFile(filePath: String) {
        rawCsvData = readCsvFile(filePath)
    }

    /**
     * Method to define the conversion factor for absolute euro amounts.
     * For example if all euro amounts are reported in millions set the value to "1000000"
     */
    fun setEuroUnitConverter(conversionFactor: String) {
        euroUnitConverter = conversionFactor
    }

    /**
     * Method to get a list of CompanyInformation objects generated from the csv file
     */
    fun buildListOfCompanyInformation(): List<CompanyInformation> {
        return rawCsvData.filter { validateLine(it) }.map {
            CompanyInformation(
                companyName = getValue("companyName", it),
                headquarters = getValue("headquarters", it),
                sector = getValue("sector", it),
                marketCap = getScaledValue("marketCap", it, euroUnitConverter)!!,
                reportingDateOfMarketCap = LocalDate.parse(
                    getValue("reportingDateOfMarketCap", it),
                    DateTimeFormatter.ofPattern("d.M.yyyy")
                ),
                identifiers = getCompanyIdentifiers(it),
                indices = getStockIndices(it)
            )
        }
    }

    private fun validateLine(csvLineData: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return (
            getValue("companyType", csvLineData) !in listOf("FS", notAvailableString) &&
                getValue("marketCap", csvLineData) != notAvailableString
            )
    }

    private fun getValue(property: String, csvData: Map<String, String>): String {
        return csvData[columnMapping[property]]!!.trim().ifBlank {
            notAvailableString
        }
    }

    private fun getScaledValue(property: String, csvData: Map<String, String>, scaleFactor: String): BigDecimal? {
        // The numeric value conversion assumes "," as decimal separator and "." to separate thousands
        return getValue(property, csvData).replace("[^,\\d]".toRegex(), "").replace(",", ".")
            .toBigDecimalOrNull()?.multiply(scaleFactor.toBigDecimal())
    }

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().sortedBy { it.name }.map {
            CompanyIdentifier(identifierValue = getValue(it.name, csvLineData), identifierType = it)
        }.filter { it.identifierValue != notAvailableString }
    }

    private fun getStockIndices(csvLineData: Map<String, String>): Set<StockIndex> {
        return StockIndex.values().filter { csvLineData[columnMapping[it.name]]!!.isNotBlank() }.toSet()
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
        return if (getValue("reportObligation", csvLineData) == "Ja") {
            YesNo.Yes
        } else {
            YesNo.No
        }
    }

    private fun getAttestation(csvLineData: Map<String, String>): AttestationOptions {
        return when (getValue("attestation", csvLineData)) {
            "reasonable" -> AttestationOptions.ReasonableAssurance
            "limited" -> AttestationOptions.LimitedAssurance
            else -> AttestationOptions.None
        }
    }

    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            totalAmount = getNumericValue("total$type", csvLineData),
            alignedPercentage = getNumericValue("aligned$type", csvLineData),
            eligiblePercentage = getNumericValue("eligible$type", csvLineData)
        )
    }

    private fun getNumericValue(property: String, csvLineData: Map<String, String>): BigDecimal? {
        return if (getValue(property, csvLineData).contains("%")) {
            getScaledValue(property, csvLineData, "0.01")
        } else {
            getScaledValue(property, csvLineData, euroUnitConverter)
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
            val converter = CsvToJsonConverter()
            // euro amounts in real data csv is expected to be in units of millions
            converter.setEuroUnitConverter("1000000")
            converter.parseCsvFile(File(args.first()).path)
            converter.writeJson()
        }
    }
}
