package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.checkIfFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import java.math.BigDecimal

/**
 * This class provides parsing methods for columns that are required by both EU-Taxonomy frameworks
 * but don't belong to the companyInformation parser
 */
class EuTaxonomyCommonFieldParser {

    companion object {
        private const val REPORT_OBLIGATION_YES = "Yes"
        private const val REPORT_OBLIGATION_NO = "No"
        private const val REPORT_OBLIGATION_NA = "n/a"
    }

    private val columnMappingEuTaxonomyUtils = mapOf(

        "companyType" to "IS/FS",
        "fiscalYear" to "Fiscal Year",
        "fiscalYearEnd" to "Fiscal Year End",
        "scopeOfEntities" to "Scope of Entities",
        "reportObligation" to "NFRD mandatory",
        "activityLevelReporting" to "EU Taxonomy Activity Level Reporting",
        "assurance" to "Assurance"

    )

    /**
     * Returns the relevant company type for the EU-Taxonomy framework (IS/FS)
     */
    fun getCompanyType(csvLineData: Map<String, String>): String? {
        return columnMappingEuTaxonomyUtils.getCsvValue("companyType", csvLineData)
            ?: throw IllegalArgumentException("No Company type listed")
    }

    /**
     * This function parses the reportingObligation field from the EU-Taxonomy framework CSV file
     */
    fun getReportingObligation(csvLineData: Map<String, String>): YesNo {
        return when (
            val rawReportObligation = columnMappingEuTaxonomyUtils.getCsvValue("reportObligation", csvLineData)
        ) {
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

    /**
     * Callable function generating the string-maps for each single DataPoint (or AssuranceData)
     */
    private fun buildMapForSpecificData(generalMap: Map<String, String>, baseString: String): Map<String, String> {
        return mapOf(
            baseString to generalMap.getValue(baseString),
            "${baseString}Quality" to "${generalMap.getValue(baseString)} Quality",
            "${baseString}Provider" to "${generalMap.getValue(baseString)} Provider",
            "${baseString}Report" to "${generalMap.getValue(baseString)} Report",
            "${baseString}Page" to "${generalMap.getValue(baseString)} Page",
        )
    }

    /**
     * parses Company reference for one single DataPoint (if existing)
     */
    private fun buildSingleCompanyReportReference(generalMap: Map<String, String>, row: Map<String, String>, baseString: String): CompanyReportReference? {
        return if (buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue("${baseString}Report", row)) {
            CompanyReportReference(
                report = buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Report", row)
                    ?: throw IllegalArgumentException("Expected a report but found null; This should not happen, since a previous check occurs"),
                page = buildMapForSpecificData(generalMap, baseString).getNumericCsvValue("${baseString}Page", row)
            )
        } else {
            null
        }
    }

    /**
     * parses one single DataPoint (if existing)
     */
    fun buildSingleDataPoint(generalMap: Map<String, String>, row: Map<String, String>, baseString: String): DataPoint<BigDecimal>? {
        return if (buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue(baseString, row)) {
            DataPoint(
                value = buildMapForSpecificData(generalMap, baseString).getNumericCsvValue(baseString, row),
                quality = QualityOptions.valueOf(
                    buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Quality", row)
                        ?: throw IllegalArgumentException(
                            "The quality of the DataPoint ${generalMap.getValue(baseString)} is" +
                                " ${buildMapForSpecificData(generalMap,baseString).getCsvValue("${baseString}Quality", row)}," +
                                " which is not a valid Quality Option"
                        )
                ),
                dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }

    /**
     * parses one single AssuranceData (if existing)
     */
    fun buildSingleAssuranceData(row: Map<String, String>): AssuranceData? {
        var baseString = "assurance"
        var generalMap = columnMappingEuTaxonomyUtils
        return if (buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue(baseString, row)) {
            AssuranceData(
                assurance = AssuranceOptions.valueOf(
                    buildMapForSpecificData(generalMap, baseString).getCsvValue(baseString, row)
                        ?: throw IllegalArgumentException(
                            "Expected an AssuranceOption but found" +
                                " ${buildMapForSpecificData(generalMap,baseString).getCsvValue(baseString, row)}" +
                                "which is not a valid Quality Option"
                        )
                ),
                provider = buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}provider", row),
                dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }
}
