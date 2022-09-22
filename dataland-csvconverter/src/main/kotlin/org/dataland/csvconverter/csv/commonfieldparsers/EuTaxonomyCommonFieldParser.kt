package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import java.time.LocalDate

/**
 * This class provides parsing methods for columns that are required by both EU-Taxonomy frameworks
 * but don't belong to the companyInformation parser
 */
class EuTaxonomyCommonFieldParser {

    companion object {
        private const val REPORT_OBLIGATION_YES = "Yes"
        private const val REPORT_OBLIGATION_NO = "No"

        private const val SCOPE_OF_ENTITIES_YES = "Yes"
        private const val SCOPE_OF_ENTITIES_NO = "No"
        private const val SCOPE_OF_ENTITIES_NA = "n/a"

        private const val ACTIVITY_LEVEL_REPORTING_YES = "Y"
        private const val ACTIVITY_LEVEL_REPORTING_NO = "N"
    }

    private val columnMappingEuTaxonomyUtils = mapOf(

        "companyType" to "IS/FS",
        "fiscalYearDeviation" to "Fiscal Year",
        "fiscalYearEnd" to "Fiscal Year End",
        "scopeOfEntities" to "Scope of Entities",
        "reportObligation" to "NFRD mandatory",
        "activityLevelReporting" to "EU Taxonomy Activity Level Reporting",

    )

    /**
     * Returns the relevant company type for the EU-Taxonomy framework (IS/FS)
     */

    private val companyTypeMap = mapOf(
        "1" to "IS",
        "2" to "FS"
    )

    fun getCompanyType(csvLineData: Map<String, String>): String {
        val companyTypeNumeric = columnMappingEuTaxonomyUtils.getCsvValue("companyType", csvLineData)
        return companyTypeMap.getValue(companyTypeNumeric ?: throw IllegalArgumentException("No Company type listed"))
    }

    /**
     * Returns the information about the companies fiscal year (End date and whether it deviates from a normal calendar year)
     */

    private val fiscalYearDeviationMap = mapOf(
        "No deviation" to YesNo.No,
        "Deviation" to YesNo.Yes
    )

    fun getFiscalYearDeviation(csvLineData: Map<String, String>): YesNo? {
        val fiscalYearDeviationString = columnMappingEuTaxonomyUtils.getCsvValue("fiscalYearDeviation", csvLineData)
        return fiscalYearDeviationMap[fiscalYearDeviationString]
    }

    fun getFiscalYearEnd(csvLineData: Map<String, String>): LocalDate? {
        val fiscalYearEndString = columnMappingEuTaxonomyUtils.getCsvValue("fiscalYearEnd", csvLineData)
        return if (fiscalYearEndString.isNullOrBlank())
            null
        else
            LocalDate.parse(fiscalYearEndString)
    }

    fun getScopeOfEntities(csvLineData: Map<String, String>): YesNoNa? {
        return when (
            val rawScopeOfEntities = columnMappingEuTaxonomyUtils.getCsvValue("scopeOfEntities", csvLineData)
        ) {
            SCOPE_OF_ENTITIES_YES -> YesNoNa.Yes
            SCOPE_OF_ENTITIES_NO -> YesNoNa.No
            SCOPE_OF_ENTITIES_NA -> YesNoNa.NA
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine Scope of Entities : Found $rawScopeOfEntities, " +
                        "but expect one of $SCOPE_OF_ENTITIES_YES, $SCOPE_OF_ENTITIES_NO, $SCOPE_OF_ENTITIES_NA or null"
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
            REPORT_OBLIGATION_YES -> YesNo.Yes
            REPORT_OBLIGATION_NO -> YesNo.No
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine reportObligation: Found $rawReportObligation, " +
                        "but expect one of $REPORT_OBLIGATION_YES, $REPORT_OBLIGATION_NO  or null"
                )
            }
        }
    }

    fun getActivityLevelReporting(csvLineData: Map<String, String>): YesNo? {
        return when (
            val rawActivityLevelReporting = columnMappingEuTaxonomyUtils.getCsvValue("activityLevelReporting", csvLineData)
        ) {
            ACTIVITY_LEVEL_REPORTING_YES -> YesNo.Yes
            ACTIVITY_LEVEL_REPORTING_NO -> YesNo.No
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine Activity Level Reporting: Found $rawActivityLevelReporting, " +
                        "but expect one of $ACTIVITY_LEVEL_REPORTING_YES, $ACTIVITY_LEVEL_REPORTING_NO or null"
                )
            }
        }
    }
}
