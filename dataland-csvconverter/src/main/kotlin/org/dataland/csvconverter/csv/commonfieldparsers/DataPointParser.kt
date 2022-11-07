package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfAnyFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValueAllowingNull
import org.dataland.csvconverter.csv.CsvUtils.readCsvDecimal
import org.dataland.csvconverter.csv.CsvUtils.readCsvLong
import org.dataland.csvconverter.csv.CsvUtils.readCsvPercentage
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

    private fun buildMapForSpecificDatapoint(generalMap: Map<String, String>, baseString: String): Map<String, String> {
        return mapOf(
            baseString to generalMap.getValue(baseString),
            "${baseString}Quality" to "${generalMap.getValue(baseString)} Quality",
            "${baseString}Comment" to "${generalMap.getValue(baseString)} Comment",
        ) + buildMapForSpecificReport(generalMap, baseString)
    }

    private fun buildMapForSpecificReport(generalMap: Map<String, String>, baseString: String): Map<String, String> {
        return mapOf(
            "${baseString}Report" to "${generalMap.getValue(baseString)} Report",
            "${baseString}Page" to "${generalMap.getValue(baseString)} Page",
            "${baseString}Tag" to "${generalMap.getValue(baseString)} Tag",
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
        val reportColumnMapping = buildMapForSpecificReport(generalMap, baseString)
        if (!reportColumnMapping.checkIfAnyFieldHasValue(reportColumnMapping.keys.toList(), row))
            return null

        return CompanyReportReference(
            report = reportColumnMapping
                .getCsvValueAllowingNull("${baseString}Report", row)
                ?.let { companyReportParser.getReverseReportNameMapping(it) }
                ?: throw IllegalArgumentException(
                    "Expected a report for $baseString as a corresponding page" +
                        " has been specified but no report was found"
                ),
            page = buildMapForSpecificDatapoint(generalMap, baseString)
                .readCsvLong("${baseString}Page", row),
            tagName = buildMapForSpecificDatapoint(generalMap, baseString)
                .getCsvValueAllowingNull("${baseString}Tag", row)
        )
    }

    private fun <T> buildDataPoint(
        generalMap: Map<String, String>,
        row: Map<String, String>,
        baseString: String,
        valueFunction: (datapointColumnMapping: Map<String, String>) -> T?
    ): DataPoint<T>? {
        val datapointColumnMapping = buildMapForSpecificDatapoint(generalMap, baseString)
        if (!datapointColumnMapping.checkIfAnyFieldHasValue(datapointColumnMapping.keys.toList(), row))
            return null

        return DataPoint(
            value = valueFunction(datapointColumnMapping),
            quality = datapointColumnMapping
                .getCsvValueAllowingNull("${baseString}Quality", row)
                .let { qualityOptionCsvParser.parse("${baseString}Quality", it) },
            comment = datapointColumnMapping
                .getCsvValueAllowingNull("${baseString}Comment", row),
            dataSource = buildSingleCompanyReportReference(generalMap, row, baseString)
        )
    }

    /**
     * Parses a single decimal data point
     */
    fun buildDecimalDataPoint(
        generalMap: Map<String, String>,
        row: Map<String, String>,
        baseString: String,
        valueScaleFactor: BigDecimal
    ):
        DataPoint<BigDecimal>? {
        return buildDataPoint(generalMap, row, baseString) {
            it.readCsvDecimal(baseString, row, valueScaleFactor)
        }
    }

    /**
     * Parses a single percentage data point
     */
    fun buildPercentageDataPoint(
        generalMap: Map<String, String>,
        row: Map<String, String>,
        baseString: String,
    ):
        DataPoint<BigDecimal>? {
        return buildDataPoint(generalMap, row, baseString) {
            it.readCsvPercentage(baseString, row)
        }
    }
}
