package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import java.math.BigDecimal

/**
 * This class provides parsing methods for columns that are required by both EU-Taxonomy frameworks
 * but don't belong to the companyInformation parser
 */
class EuTaxonomyCommonFieldParser {

    companion object {
        const val STRING_YES = "Yes"
        const val STRING_NO = "No"
        const val STRING_NA = "N/A"
    }

    private val columnMappingEuTaxonomyUtils = mapOf(
        "scopeOfEntities" to "Scope of Entities",
        "reportObligation" to "NFRD mandatory",
        "activityLevelReporting" to "EU Taxonomy Activity Level Reporting",
        "numberOfEmployees" to "Number Of Employees",
    )

    /**
     * This method retrieves scope of companies from a csv row
     */
    fun getScopeOfEntities(csvLineData: Map<String, String>): YesNoNa? {
        return when (
            val rawScopeOfEntities = columnMappingEuTaxonomyUtils.getCsvValue("scopeOfEntities", csvLineData)
        ) {
            STRING_YES -> YesNoNa.Yes
            STRING_NO -> YesNoNa.No
            STRING_NA -> YesNoNa.NA
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine Scope of Entities : Found $rawScopeOfEntities, " +
                        "but expect one of $STRING_YES, $STRING_NO, $STRING_NA or null"
                )
            }
        }
    }

    /**
     * This function parses the reportingObligation field from the EU-Taxonomy framework CSV file
     */
    fun getReportingObligation(csvLineData: Map<String, String>): YesNo? {
        return when (
            val rawReportObligation = columnMappingEuTaxonomyUtils.getCsvValue("reportObligation", csvLineData)
        ) {
            STRING_YES -> YesNo.Yes
            STRING_NO -> YesNo.No
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine reportObligation: Found $rawReportObligation, " +
                        "but expect one of $STRING_YES, $STRING_NO  or null"
                )
            }
        }
    }

    /**
     * This method retrieves the activity level reporting
     */
    fun getActivityLevelReporting(csvLineData: Map<String, String>): YesNo? {
        return when (
            val rawActivityLevelReporting = columnMappingEuTaxonomyUtils.getCsvValue(
                "activityLevelReporting",
                csvLineData
            )
        ) {
            STRING_YES -> YesNo.Yes
            STRING_NO -> YesNo.No
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine Activity Level Reporting: Found $rawActivityLevelReporting, " +
                        "but expect one of $STRING_YES, $STRING_NO or null"
                )
            }
        }
    }

    /**
     * Returns the number of employees for a specific company
     */
    fun getNumberOfEmployees(csvLineData: Map<String, String>): BigDecimal? {
        return columnMappingEuTaxonomyUtils.getNumericCsvValue("numberOfEmployees", csvLineData)
    }
}
