package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.csvconverter.csv.utils.EnumCsvParser
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

    private val qualityOptionCsvParser = EnumCsvParser<QualityOptions>(
        mapOf(
            "Audited" to QualityOptions.Audited,
            "Reported" to QualityOptions.Reported,
            "Estimated" to QualityOptions.Estimated,
            "Incomplete" to QualityOptions.Incomplete,
            "N/A" to QualityOptions.NA
        )
    )

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
                quality = buildMapForSpecificData(generalMap, baseString)
                    .getCsvValue("${baseString}Quality", row)
                    .let { qualityOptionCsvParser.parse("${baseString}Quality", it) },
                comment = buildMapForSpecificData(generalMap, baseString)
                    .getCsvValue("${baseString}Comment", row),
                dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }
}
