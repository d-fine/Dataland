package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CompanyReportParser {

    private val columnMapReportTitles = mapOf(
            "AnnualReport" to "Annual Report",
            "SustainabilityReport" to "Sustainability Report",
            "IntegratedReport" to "Integrated Report",
            "ESEFReport" to "ESEF Report",
    )

    fun buildMapOfAllCompanyReports(csvLineData: Map<String, String>): Map<String, CompanyReport> {
        return columnMapReportTitles
                .mapNotNull { (key, _) -> buildSingleCompanyReport(csvLineData, key)?.let {key to it} }
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
            baseString: String
    ): CompanyReport? {
        val report = buildMapForSpecificReport(baseString).getCsvValue(baseString, csvLineData)
        return if (report !== null) {
            CompanyReport(
                    reference = report,
                    isGroupLevel = getIsGroupLevelAttribute(csvLineData, baseString, report),
                    reportDate = buildMapForSpecificReport(baseString).getCsvValue(
                            "${baseString}Date", csvLineData)?.let { LocalDate.parse(it,  DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
                           ,
                    currency = buildMapForSpecificReport(baseString).getCsvValue(
                            "${baseString}Currency", csvLineData)
            )
        } else {
            null
        }
    }

    private fun getIsGroupLevelAttribute(csvLineData: Map<String, String>,
                                         baseString: String,
                                         report: String): YesNoNa? {
        return when (
            val rawIsGroupLevelAttribute = buildMapForSpecificReport(baseString).getCsvValue(
                    "${baseString}GroupLevel",
                    csvLineData
            )
        ) {
            EuTaxonomyCommonFieldParser.STRING_YES -> YesNoNa.Yes
            EuTaxonomyCommonFieldParser.STRING_NO -> YesNoNa.No
            EuTaxonomyCommonFieldParser.STRING_NA -> YesNoNa.NA
            null -> null
            else -> {
                throw java.lang.IllegalArgumentException(
                        "Could not determine Group Level Attribute of the report $report: Found $rawIsGroupLevelAttribute, " +
                                "but expect one of ${EuTaxonomyCommonFieldParser.STRING_YES}," +
                                " ${EuTaxonomyCommonFieldParser.STRING_NO}," +
                                " ${EuTaxonomyCommonFieldParser.STRING_NA} or null"
                )
            }
        }
    }
}