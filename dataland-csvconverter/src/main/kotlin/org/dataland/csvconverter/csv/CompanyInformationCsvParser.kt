package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getScaledCsvValue
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
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
        "industry" to "Industry",
        "currency" to "Currency",
        "marketCap" to "Market Capitalization EURmm",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
        "numberOfShares" to "Number Of Shares",
        "sharePrice" to "Share Price",
        "numberOfEmployees" to "Number Of Employees",
        IdentifierType.Isin.name to "ISIN",
        IdentifierType.Lei.name to "LEI",
        IdentifierType.PermId.name to "PermID",
        IdentifierType.DunsNumber.name to "D-U-N-S Number",
        IdentifierType.Ticker.name to "Ticker",
        "isTeaserCompany" to "Teaser Company",
    )

    /**
     * Method to build CompanyInformation from the read row in the csv file.
     */
    fun buildCompanyInformation(row: Map<String, String>): CompanyInformation {
        return CompanyInformation(
            companyName = companyInformationColumnMapping.getCsvValue("companyName", row)!!,
            headquarters = companyInformationColumnMapping.getCsvValue("headquarters", row)!!,
            sector = companyInformationColumnMapping.getCsvValue("sector", row)!!,
            industry = companyInformationColumnMapping.getCsvValue("industry", row),
            currency = companyInformationColumnMapping.getCsvValue("currency", row),
            marketCap = getMarketCap(row),
            reportingDateOfMarketCap = LocalDate.parse(
                companyInformationColumnMapping.getCsvValue("reportingDateOfMarketCap", row),
                DateTimeFormatter.ofPattern("d.M.yyyy")
            ),
            numberOfShares = companyInformationColumnMapping.getScaledCsvValue("numberOfShares", row, "1"),
            sharePrice = companyInformationColumnMapping.getScaledCsvValue("sharePrice", row, "1"),
            numberOfEmployees = companyInformationColumnMapping.getScaledCsvValue("numberOfEmployees", row, "1"),
            identifiers = getCompanyIdentifiers(row),
            countryCode = companyInformationColumnMapping.getCsvValue("countryCode", row)!!,
            isTeaserCompany = companyInformationColumnMapping.getCsvValue("isTeaserCompany", row)
                .equals("Yes", true)
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

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().sortedBy { it.name }.map {
            CompanyIdentifier(
                identifierValue = companyInformationColumnMapping.getCsvValue(it.name, csvLineData)!!,
                identifierType = it
            )
        }.filter { it.identifierValue != CsvUtils.NOT_AVAILABLE_STRING }
    }
}
