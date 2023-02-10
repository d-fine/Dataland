package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValueAllowingNull
import org.dataland.csvconverter.csv.CsvUtils.readMultiValuedCsvField
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/**
 * This class is responsible for extracting basic company information from a CSV row
 */
class CompanyInformationCsvParser {

    private val companyInformationColumnMapping = mapOf(
        "companyName" to "Unternehmensname",
        "companyAlternativeNames" to "Alternative Names",
        "companyLegalForm" to "Company Legal Form",
        "headquarters" to "Headquarter",
        "headquartersPostalCode" to "Headquarter Postal Code",
        "countryCode" to "Countrycode",
        "sector" to "Sector",
        IdentifierType.Isin.name to "ISIN",
        IdentifierType.Lei.name to "LEI",
        IdentifierType.PermId.name to "PermID",
        IdentifierType.Ticker.name to "Ticker",
        IdentifierType.Duns.name to "DUNS",
        IdentifierType.CompanyRegistrationNumber.name to "Company Registration Number",
        "isTeaserCompany" to "Teaser Company",
        "website" to "Website"
    )

    /**
     * Method to build CompanyInformation from the read row in the csv file.
     */
    fun buildCompanyInformation(row: Map<String, String>): CompanyInformation {
        return CompanyInformation(
            companyName = companyInformationColumnMapping.getCsvValue("companyName", row),
            companyAlternativeNames = companyInformationColumnMapping
                .readMultiValuedCsvField("companyAlternativeNames", row),
            companyLegalForm = companyInformationColumnMapping.getCsvValueAllowingNull("companyLegalForm", row),
            headquarters = companyInformationColumnMapping.getCsvValue("headquarters", row),
            headquartersPostalCode = companyInformationColumnMapping
                .getCsvValueAllowingNull("headquartersPostalCode", row),
            sector = companyInformationColumnMapping.getCsvValue("sector", row),
            identifiers = getCompanyIdentifiers(row),
            countryCode = companyInformationColumnMapping.getCsvValue("countryCode", row),
            isTeaserCompany = companyInformationColumnMapping.getCsvValueAllowingNull("isTeaserCompany", row)
                .equals("Yes", true),
            website = companyInformationColumnMapping.getCsvValueAllowingNull("website", row)
        )
    }

    private fun getCompanyIdentifiers(csvLineData: Map<String, String>): List<CompanyIdentifier> {
        return IdentifierType.values().mapNotNull { identifierType ->
            companyInformationColumnMapping.getCsvValueAllowingNull(identifierType.name, csvLineData)?.let {
                CompanyIdentifier(
                    identifierValue = it,
                    identifierType = identifierType,
                )
            }
        }.sortedBy { it.identifierType.name }
    }
}
