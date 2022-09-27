package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.readCsvDecimal
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import java.math.BigDecimal

/**
 * This class provides parsing methods for columns that are required by both EU-Taxonomy frameworks
 * but don't belong to the companyInformation parser
 */
class EuTaxonomyCommonFieldParser(
    private val yesNoNaParser: EnumCsvParser<YesNoNa>,
    private val yesNoParser: EnumCsvParser<YesNo>
) {
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
        return columnMappingEuTaxonomyUtils.getCsvValue("scopeOfEntities", csvLineData)
            ?.let { yesNoNaParser.parseAllowingNull("scopeOfEntities", it) }
    }

    /**
     * This function parses the reportingObligation field from the EU-Taxonomy framework CSV file
     */
    fun getReportingObligation(csvLineData: Map<String, String>): YesNo? {
        return columnMappingEuTaxonomyUtils.getCsvValue("reportObligation", csvLineData)
            ?.let { yesNoParser.parseAllowingNull("reportObligation", it) }
    }

    /**
     * This method retrieves the activity level reporting
     */
    fun getActivityLevelReporting(csvLineData: Map<String, String>): YesNo? {
        return columnMappingEuTaxonomyUtils.getCsvValue("activityLevelReporting", csvLineData)
            ?.let { yesNoParser.parseAllowingNull("activityLevelReporting", it) }
    }

    /**
     * Returns the number of employees for a specific company
     */
    fun getNumberOfEmployees(csvLineData: Map<String, String>): BigDecimal? {
        return columnMappingEuTaxonomyUtils.readCsvDecimal("numberOfEmployees", csvLineData)
    }
}
