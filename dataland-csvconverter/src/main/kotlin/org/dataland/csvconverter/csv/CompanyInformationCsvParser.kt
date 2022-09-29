package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValueAllowingNull
import org.dataland.csvconverter.csv.CsvUtils.readCsvDecimal
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
        "marketCap" to "Market Capitalization EURmm",
        "reportingDateOfMarketCap" to "Market Capitalization Date",
        IdentifierType.Isin.name to "ISIN",
        IdentifierType.Lei.name to "LEI",
        IdentifierType.PermId.name to "PermID",
        "isTeaserCompany" to "Teaser Company",
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
                companyInformationColumnMapping.getCsvValueAllowingNull("reportingDateOfMarketCap", row),
                DateTimeFormatter.ofPattern("d.M.yyyy")
            ),
            identifiers = getCompanyIdentifiers(row),
            countryCode = companyInformationColumnMapping.getCsvValue("countryCode", row),
            isTeaserCompany = companyInformationColumnMapping.getCsvValueAllowingNull("isTeaserCompany", row)
                .equals("Yes", true)
        )
    }

    private fun getMarketCap(csvLineData: Map<String, String>): BigDecimal {
        return companyInformationColumnMapping.readCsvDecimal(
            "marketCap",
            csvLineData,
            CsvUtils.SCALE_FACTOR_ONE_MILLION
        ) ?: throw IllegalArgumentException(
            "Could not parse market capitalisation for company \"${
            companyInformationColumnMapping.getCsvValueAllowingNull("companyName", csvLineData)}\""
        )
    }

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().mapNotNull { identifierType ->
            companyInformationColumnMapping.getCsvValueAllowingNull(identifierType.name, csvLineData)?.let {
                CompanyIdentifier(
                    identifierValue = it,
                    identifierType = identifierType
                )
            }
        }.sortedBy { it.identifierType.name }
    }
}
