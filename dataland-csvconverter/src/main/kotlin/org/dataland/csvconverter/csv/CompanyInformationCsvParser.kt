package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getScaledCsvValue
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.model.enums.company.StockIndex
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * This class is responsible for extracting basic company information from a CSV row
 */
class CompanyInformationCsvParser {

    private val companyInformationColumnMapping = mapOf(
        "companyName" to "Unternehmensname",
        "headquarters" to "Headquarter",
        "countryCode" to "Countrycode",
        "sector" to "Sector",
        "marketCap" to "Market Capitalization EURmm",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
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

    /**
     * Method to build CompanyInformation from the read row in the csv file.
     */
    fun buildCompanyInformation(row: Map<String, String>): CompanyInformation {
        return CompanyInformation(
            companyName = companyInformationColumnMapping.getCsvValue("companyName", row),
            headquarters = companyInformationColumnMapping.getCsvValue("headquarters", row),
            sector = companyInformationColumnMapping.getCsvValue("sector", row),
            marketCap = getMarketCap(row),
            reportingDateOfMarketCap = LocalDate.parse(
                companyInformationColumnMapping.getCsvValue("reportingDateOfMarketCap", row),
                DateTimeFormatter.ofPattern("d.M.yyyy")
            ),
            identifiers = getCompanyIdentifiers(row),
            indices = getStockIndices(row),
            countryCode = companyInformationColumnMapping.getCsvValue("countryCode", row),
            isTeaserCompany = false
        )
    }

    private fun getMarketCap(csvLineData: Map<String, String>): BigDecimal {
        return companyInformationColumnMapping.getScaledCsvValue(
            "marketCap",
            csvLineData,
            CsvUtils.EURO_UNIT_CONVERSION_FACTOR
        ) ?: throw IllegalArgumentException(
            "Could not parse market capitalisation for company \"${
            companyInformationColumnMapping.getCsvValue("companyName", csvLineData)}\""
        )
    }

    private fun getStockIndices(csvLineData: Map<String, String>): Set<StockIndex> {
        return StockIndex.values().filter {
            (csvLineData[companyInformationColumnMapping[it.name]] ?: "")
                .isNotBlank()
        }.toSet()
    }

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().sortedBy { it.name }.map {
            CompanyIdentifier(
                identifierValue = companyInformationColumnMapping.getCsvValue(it.name, csvLineData),
                identifierType = it
            )
        }.filter { it.identifierValue != CsvUtils.NOT_AVAILABLE_STRING }
    }
}
