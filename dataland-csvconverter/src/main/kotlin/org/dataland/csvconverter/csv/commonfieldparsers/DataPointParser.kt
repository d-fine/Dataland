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
class DataPointParser {

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
        return if (buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue("${baseString}Report", row)) {
            CompanyReportReference(
                report = buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Report", row)
                    ?: throw IllegalArgumentException(
                        "Expected a report but found null; This should not happen," +
                            " since a previous check occurs"
                    ),
                page = buildMapForSpecificData(generalMap, baseString).getNumericCsvValue("${baseString}Page", row)
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
        return if (buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue(baseString, row)) {
            DataPoint(
                value = buildMapForSpecificData(generalMap, baseString).getNumericCsvValue(baseString, row),
                quality = QualityOptions.valueOf(
                    buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Quality", row)
                        ?: throw IllegalArgumentException(
                            "The quality of the DataPoint ${generalMap.getValue(baseString)} is" +
                                " ${buildMapForSpecificData(generalMap,baseString).getCsvValue(
                                    "${baseString}Quality", row
                                )}," +
                                " which is not a valid Quality Option"
                        )
                ),
                comment = buildMapForSpecificData(generalMap, baseString).getCsvValue("${baseString}Comment", row),
                dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }
}
