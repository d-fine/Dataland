package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigDecimal

/**
 * This class provides methods to create data points
 */
class DataPointParser(
    private val companyReportParser: CompanyReportParser
) {

    companion object {
        const val QUALITYOPTION_AUDITED = "Audited"
        const val QUALITYOPTION_REPORTED = "Reported"
        const val QUALITYOPTION_ESTIMATED = "Estimated"
        const val QUALITYOPTION_INCOMPLETE = "Incomplete"
        const val QUALITYOPTION_NA = EuTaxonomyCommonFieldParser.STRING_NA
    }

    /**
     * Callable function generating the string-maps for each single DataPoint (or AssuranceData)
     */
    fun buildMapForSpecificData(generalMap: Map<String, String>, baseString: String): Map<String, String> {
        return mapOf(
            baseString to generalMap.getValue(baseString),
            "${baseString}Quality" to "${generalMap.getValue(baseString)} Quality",
            "${baseString}Provider" to "${generalMap.getValue(baseString)} Provider",
            "${baseString}Report" to "${generalMap.getValue(baseString)} Report",
            "${baseString}Page" to "${generalMap.getValue(baseString)} Page",
            "${baseString}Comment" to "${generalMap.getValue(baseString)} Comment",
        )
    }

    /**
     * parses Company reference for one single DataPoint (if existing)
     */
    fun buildSingleCompanyReportReference(
        generalMap: Map<String, String>,
        row: Map<String, String>,
        baseString: String
    ): CompanyReportReference? {
        return if (buildMapForSpecificData(generalMap, baseString)
            .checkIfFieldHasValue("${baseString}Report", row)
        ) {
            CompanyReportReference(
                report = buildMapForSpecificData(generalMap, baseString)
                    .getCsvValue("${baseString}Report", row)
                    ?.let { companyReportParser.getReverseReportNameMapping(it) }
                    ?: throw IllegalArgumentException(
                        "Expected a report but found null; This should not happen," +
                            " since a previous check occurs"
                    ),
                page = buildMapForSpecificData(generalMap, baseString)
                    .getNumericCsvValue("${baseString}Page", row)
            )
        } else {
            null
        }
    }

    private fun getQualityOption(value: String?): QualityOptions {
        return when (
            value
        ) {
            QUALITYOPTION_AUDITED -> QualityOptions.Audited
            QUALITYOPTION_REPORTED -> QualityOptions.Reported
            QUALITYOPTION_ESTIMATED -> QualityOptions.Estimated
            QUALITYOPTION_INCOMPLETE -> QualityOptions.Incomplete
            QUALITYOPTION_NA -> QualityOptions.NA
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine reportObligation: Found $value, " +
                        "but expect one of ${EuTaxonomyCommonFieldParser.STRING_YES}," +
                        " ${EuTaxonomyCommonFieldParser.STRING_NO}  or null"
                )
            }
        }
    }

    /**
     * parses one single DataPoint (if existing)
     */
    fun buildSingleDataPoint(generalMap: Map<String, String>, row: Map<String, String>, baseString: String):
        DataPoint<BigDecimal>? {
        return if (buildMapForSpecificData(generalMap, baseString)
            .checkIfFieldHasValue("${baseString}Quality", row)
        ) {
            DataPoint(
                value = buildMapForSpecificData(generalMap, baseString).getNumericCsvValue(baseString, row),
                quality = getQualityOption(
                    buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Quality", row)
                ),
                comment = buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Comment", row),
                dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }
}
