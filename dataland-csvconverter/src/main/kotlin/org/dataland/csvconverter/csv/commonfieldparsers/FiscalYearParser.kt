package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import java.time.LocalDate

class FiscalYearParser {
    /**
     * Returns the information about the companies fiscal year (End date and whether it deviates from a normal calendar year)
     */
    private val columnMappingFiscalYear = mapOf(
        "fiscalYearDeviation" to "Fiscal Year",
        "fiscalYearEnd" to "Fiscal Year End",
    )
    private val fiscalYearDeviationMap = mapOf(
        "No deviation" to YesNo.No,
        "Deviation" to YesNo.Yes
    )

    fun getFiscalYearDeviation(csvLineData: Map<String, String>): YesNo? {
        val fiscalYearDeviationString = columnMappingFiscalYear.getCsvValue("fiscalYearDeviation", csvLineData)
        return fiscalYearDeviationMap[fiscalYearDeviationString]
    }

    fun getFiscalYearEnd(csvLineData: Map<String, String>): LocalDate? {
        val fiscalYearEndString = columnMappingFiscalYear.getCsvValue("fiscalYearEnd", csvLineData)
        return if (fiscalYearEndString.isNullOrBlank())
            null
        else
            LocalDate.parse(fiscalYearEndString)
    }
}
