package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.utils.EnumCsvParser

/**
 * This class provides methods to retrieve the company type of csv row
 */
class CompanyTypeParser {
    private val columnMappingCompanyType = mapOf(
        "companyType" to "IS/FS",
    )

    /**
     * Returns the relevant company type for the EU-Taxonomy framework (IS/FS)
     */
    private val companyTypeCsvParser = EnumCsvParser(
        mapOf(
            "1" to "IS",
            "2" to "FS"
        )
    )

    /**
     * This method retrieves the company type from a csv row
     */
    fun getCompanyType(csvLineData: Map<String, String>): String {
        val companyTypeNumeric = columnMappingCompanyType.getCsvValue("companyType", csvLineData)
        return companyTypeCsvParser.parse(columnMappingCompanyType["companyType"]!!, companyTypeNumeric)
    }
}
