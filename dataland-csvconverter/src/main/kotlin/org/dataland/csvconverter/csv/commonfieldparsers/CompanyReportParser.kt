package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfAnyFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValueAllowingNull
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * This class provides the method required for parsing the top-level company reports
 */
class CompanyReportParser(
    private val yesNoNaParser: EnumCsvParser<YesNoNa>,
) {

    private val columnMapReportTitles = mapOf(
        "AnnualReport" to "Annual Report",
        "SustainabilityReport" to "Sustainability Report",
        "IntegratedReport" to "Integrated Report",
        "ESEFReport" to "ESEF Report",
    )

    /**
     * Maps a human-readable report name to our internal reference name
     */
    fun getReverseReportNameMapping(reportNameFromCsv: String): String {
        return columnMapReportTitles.entries.firstOrNull { it.value == reportNameFromCsv }?.key
            ?: throw IllegalArgumentException("Unknown report $reportNameFromCsv")
    }

    /**
     * Creates the top-level company report mapping required for all frameworks
     */
    fun buildMapOfAllCompanyReports(csvLineData: Map<String, String>): Map<String, CompanyReport> {
        return columnMapReportTitles
            .mapNotNull { (key, _) -> buildSingleCompanyReport(csvLineData, key)?.let { key to it } }
            .toMap()
    }

    private fun buildMapForSpecificReport(baseString: String): Map<String, String> {
        return mapOf(
            baseString to columnMapReportTitles.getValue(baseString),
            "${baseString}GroupLevel" to "Group Level ${columnMapReportTitles.getValue(baseString)}",
            "${baseString}Date" to "${columnMapReportTitles.getValue(baseString)} Date",
            "${baseString}Currency" to "${columnMapReportTitles.getValue(baseString)} Currency",
        )
    }

    private fun buildSingleCompanyReport(
        csvLineData: Map<String, String>,
        baseString: String,
    ): CompanyReport? {
        val reportMap = buildMapForSpecificReport(baseString)
        if (!reportMap.checkIfAnyFieldHasValue(reportMap.keys.toList(), csvLineData)) {
            return null
        }

        return CompanyReport(
            reference = reportMap.getCsvValueAllowingNull(baseString, csvLineData)
                ?: throw IllegalArgumentException(
                    "Report reference for $baseString has not been defined " +
                        "but some other values have. This should not happen",
                ),
            isGroupLevel = reportMap.getCsvValueAllowingNull("${baseString}GroupLevel", csvLineData)
                .let { yesNoNaParser.parseAllowingNull("${baseString}GroupLevel", it) },
            reportDate = reportMap.getCsvValueAllowingNull("${baseString}Date", csvLineData)
                ?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
            currency = reportMap.getCsvValueAllowingNull(
                "${baseString}Currency", csvLineData,
            ),
        )
    }
}
