package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue

class ReportingPeriodParser {
    private val columnMappingReportingPeriod = mapOf(
            "reportingPeriod" to "Reporting Period"
    )

    /**
     * Returns the reporting period
     */
    fun getReportingPeriod(csvLineData: Map<String, String>): String {
        return columnMappingReportingPeriod.getCsvValue("reportingPeriod", csvLineData)
    }
}