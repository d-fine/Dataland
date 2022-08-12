package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.dataland.datalandbackend.model.*
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.dataland.datalandbackend.model.enums.eutaxonomy.AttestationOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.FinancialServicesType
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.utils.CompanyInformationWithData
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val REPORT_OBLIGATION_YES = "Yes"
private const val REPORT_OBLIGATION_NO = "No"
private const val REPORT_OBLIGATION_NA = "n/a"

private const val ATTESTATION_REASONABLE = "reasonable"
private const val ATTESTATION_LIMITED = "limited"
private const val ATTESTATION_NA = "n/a"
private const val ATTESTATION_NONE = "none"
private const val NOT_AVAILABLE_STRING = "n/a"

/**
 * Class to transform company information and EU Taxonomy data for non financials delivered by csv into json format
 */
class CsvToJsonConverter {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        .registerModule(NoTrailingZerosBigDecimalDeserializer.module)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    private var euroUnitConversionFactor = "1"
    private var rawCsvData: List<Map<String, String>> = listOf()

    private val columnMappingEuTaxonomyForNonFinancials = mapOf(
        "companyName" to "Unternehmensname",
        "headquarters" to "Headquarter",
        "countryCode" to "Countrycode",
        "sector" to "Sector",
        "marketCap" to "Market Capitalization EURmm",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
        "totalRevenue" to "Total Revenue EURmm",
        "totalCapex" to "Total CapEx EURmm",
        "totalOpex" to "Total OpEx EURmm",
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
        StockIndex.Cdax.name to "CDAX",
        StockIndex.Gex.name to "GEX",
        StockIndex.Dax.name to "DAX",
        StockIndex.Mdax.name to "MDAX",
        StockIndex.Sdax.name to "SDAX",
        StockIndex.TecDax.name to "TecDAX",
        StockIndex.Dax50Esg.name to "DAX 50 ESG"
    )

    private val columnMappingEuTaxonomyForFinancials = mapOf(
        "financialServicesType" to "FS - company type",
        "taxonomyEligibleActivity" to "Exposures to taxonomy-eligible economic activities",
        "banksAndIssuers" to "Exposures to central governments, central banks, supranational issuers",
        "derivatives" to "Exposures to derivatives",
        "investmentNonNfrd" to "Exposures to non-NFRD entities",
        "tradingPortfolio" to "Trading portfolio",
        "interbankLoans" to "On-demand interbank loans",
        "tradingPortfolio" to "Trading portfolio",
        "tradingPortfolioAndInterbankLoans" to "Trading portfolio & on demand interbank loans",
        "taxonomyEligibleNonLifeInsuranceActivities" to "Taxonomy-eligible non-life insurance economic activities",
        FinancialServicesType.CreditInstitution.name to "Credit Institution",
        FinancialServicesType.AssetManagement.name to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance.name to "Insurance/Reinsurance"
    )

    private val combinedColumnMapping = columnMappingEuTaxonomyForFinancials + columnMappingEuTaxonomyForNonFinancials

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
     * Method to build CompanyInformation from the read row in the csv file.
     */
    private fun buildCompanyInformation(row: Map<String, String>): CompanyInformation {
        return CompanyInformation(
            companyName = getValue("companyName", row),
            headquarters = getValue("headquarters", row),
            sector = getValue("sector", row),
            marketCap = getScaledValue("marketCap", row, euroUnitConversionFactor)!!,
            reportingDateOfMarketCap = LocalDate.parse(
                getValue("reportingDateOfMarketCap", row), DateTimeFormatter.ofPattern("d.M.yyyy")
            ),
            identifiers = getCompanyIdentifiers(row),
            indices = getStockIndices(row),
            countryCode = getValue("countryCode", row)
        )
    }

    /**
     * Method to build EuTaxonomyDataForNonFinancials from the read row in the csv file.
     */
    private fun buildEuTaxonomyDataForNonFinancials(row: Map<String, String>): EuTaxonomyDataForNonFinancials {
        return EuTaxonomyDataForNonFinancials(
            reportObligation = getReportingObligation(row), attestation = getAttestation(row),
            capex = buildEuTaxonomyDetailsPerCashFlowType("Capex", row),
            opex = buildEuTaxonomyDetailsPerCashFlowType("Opex", row),
            revenue = buildEuTaxonomyDetailsPerCashFlowType("Revenue", row)
        )
    }

    private fun buildEuTaxonomyDataForFinancials(row : Map<String, String>): EuTaxonomyDataForFinancials {
        return EuTaxonomyDataForFinancials(
            reportingObligation = getReportingObligation(row),
            attestation = getAttestation(row),
            financialServicesType = getFinancialServiceType(row),
            eligibilityKpis = EuTaxonomyDataForFinancials.EligibilityKpis(
                taxonomyEligibleActivity = getNumericValue("taxonomyEligibleActivity", row),
                banksAndIssuers = getNumericValue("banksAndIssuers", row),
                derivatives = getNumericValue("derivatives", row),
                investmentNonNfrd = getNumericValue("investmentNonNfrd", row),
            ),
           creditInstitutionKpis = EuTaxonomyDataForFinancials.CreditInstitutionKpis(
               interbankLoans = getNumericValue("interbankLoans", row),
               tradingPortfolio = getNumericValue("tradingPortfolio", row),
               tradingPortfolioAndInterbankLoans = getNumericValue("tradingPortfolioAndInterbankLoans", row)
           ),
            insuranceKpis = EuTaxonomyDataForFinancials.InsuranceKpis(
                taxonomyEligibleNonLifeInsuranceActivities
                = getNumericValue("taxonomyEligibleNonLifeInsuranceActivities", row),
            ),
        )
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
    fun setEuroUnitConversionFactor(conversionFactor: String) {
        euroUnitConversionFactor = conversionFactor
    }

    /**
     * Method to get a list of CompanyInformationWithEuTaxonomyDataForNonFinancials objects generated from the csv file
     */
    fun buildListOfCompanyInformationWithEuTaxonomyDataForNonFinancials():
            List<CompanyInformationWithData<EuTaxonomyDataForNonFinancials>> {
        return rawCsvData.filter { validateLineNonFinancial(it) }.map {
            CompanyInformationWithData(
                buildCompanyInformation(it),
                buildEuTaxonomyDataForNonFinancials(it)
            )
        }
    }

    /**
     * Method to get a list of CompanyInformationWithEuTaxonomyDataForFinancials objects generated from the csv file
     */
    fun buildListOfCompanyInformationWithEuTaxonomyDataForFinancials():
            List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> {
        return rawCsvData.filter { validateLineFinancial(it) }.map {
            CompanyInformationWithData(
                buildCompanyInformation(it),
                buildEuTaxonomyDataForFinancials(it)
            )
        }
    }

    private fun getFinancialServiceType(csvLineData: Map<String, String>): FinancialServicesType {
        return FinancialServicesType.values().firstOrNull {
            csvLineData[columnMappingEuTaxonomyForFinancials["financialServicesType"]].equals(columnMappingEuTaxonomyForFinancials[it.name], ignoreCase = true)
        } ?: throw IllegalArgumentException("Could not determine financial services type")
    }

    private fun validateLineNonFinancial(csvLineData: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return getValue("companyType", csvLineData) !in listOf("FS", NOT_AVAILABLE_STRING) &&
            getValue("marketCap", csvLineData) != NOT_AVAILABLE_STRING
    }

    private fun validateLineFinancial(csvLineData: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return getValue("companyType", csvLineData) == "FS" &&
                getValue("marketCap", csvLineData) != NOT_AVAILABLE_STRING &&
                // Skip Allianz until inconsistencies are resolved
                getValue("companyName", csvLineData).trim() != "Allianz SE"
    }

    private fun getValue(property: String, csvData: Map<String, String>): String {
        return csvData[combinedColumnMapping[property]!!]!!.trim().ifBlank {
            NOT_AVAILABLE_STRING
        }
    }

    private fun getScaledValue(property: String, csvData: Map<String, String>, scaleFactor: String): BigDecimal? {
        // The numeric value conversion assumes "," as decimal separator and "." to separate thousands
        return getValue(property, csvData).replace("[^,\\d]".toRegex(), "").replace(",", ".")
            .toBigDecimalOrNull()?.multiply(scaleFactor.toBigDecimal())?.stripTrailingZeros()
    }

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().sortedBy { it.name }.map {
            CompanyIdentifier(identifierValue = getValue(it.name, csvLineData), identifierType = it)
        }.filter { it.identifierValue != NOT_AVAILABLE_STRING }
    }

    private fun getStockIndices(csvLineData: Map<String, String>): Set<StockIndex> {
        return StockIndex.values().filter {
            (csvLineData[columnMappingEuTaxonomyForNonFinancials[it.name]] ?: "")
                .isNotBlank()
        }.toSet()
    }

    private fun getReportingObligation(csvLineData: Map<String, String>): YesNo {
        val rawReportObligation = getValue("reportObligation", csvLineData)
        return when (rawReportObligation) {
            REPORT_OBLIGATION_YES -> YesNo.Yes
            REPORT_OBLIGATION_NO, REPORT_OBLIGATION_NA -> YesNo.No
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine reportObligation: Found $rawReportObligation, " +
                            "but expect one of $REPORT_OBLIGATION_YES, $REPORT_OBLIGATION_NO or $REPORT_OBLIGATION_NA"
                )
            }
        }
    }

    private fun getAttestation(csvLineData: Map<String, String>): AttestationOptions {
        val rawAttestation = getValue("attestation", csvLineData)
        return when (getValue("attestation", csvLineData)) {
            ATTESTATION_REASONABLE -> AttestationOptions.ReasonableAssurance
            ATTESTATION_LIMITED -> AttestationOptions.LimitedAssurance
            ATTESTATION_NA, ATTESTATION_NONE -> AttestationOptions.None
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine attestation: Found $rawAttestation, " +
                            "but expect one of $ATTESTATION_REASONABLE, $ATTESTATION_LIMITED, " +
                            "$ATTESTATION_NA or $ATTESTATION_NONE "
                )
            }
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
            getScaledValue(property, csvLineData, euroUnitConversionFactor)
        }
    }

    /**
     * Method to write the transformed data into json for non financial companies
     */
    fun writeJsonNonFinancials() {
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(
                File("./CompanyInformationWithEuTaxonomyDataForNonFinancials.json"),
                buildListOfCompanyInformationWithEuTaxonomyDataForNonFinancials()
            )
    }

    /**
     * Method to write the transformed data into json for financial companies
     */
    fun writeJsonFinancials() {
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(
                File("./CompanyInformationWithEuTaxonomyDataForFinancials.json"),
                buildListOfCompanyInformationWithEuTaxonomyDataForFinancials()
            )
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
            converter.setEuroUnitConversionFactor("1000000")
            converter.parseCsvFile(File(args.first()).path)
            converter.writeJsonNonFinancials()
            converter.writeJsonFinancials()
        }
    }
}
