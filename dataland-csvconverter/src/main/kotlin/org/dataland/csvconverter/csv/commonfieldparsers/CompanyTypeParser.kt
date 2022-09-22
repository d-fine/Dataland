package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue

class CompanyTypeParser {
    private val columnMappingCompanyType = mapOf(
        "companyType" to "IS/FS",
    )
    /**
     * Returns the relevant company type for the EU-Taxonomy framework (IS/FS)
     */

    private val companyTypeMap = mapOf(
        "1" to "IS",
        "2" to "FS"
    )

    fun getCompanyType(csvLineData: Map<String, String>): String {
        val companyTypeNumeric = columnMappingCompanyType.getCsvValue("companyType", csvLineData)
        return companyTypeMap.getValue(companyTypeNumeric ?: throw IllegalArgumentException("No Company type listed"))
    }
}
