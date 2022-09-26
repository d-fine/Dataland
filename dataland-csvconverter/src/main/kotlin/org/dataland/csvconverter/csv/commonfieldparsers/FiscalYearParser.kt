package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import java.time.LocalDate

/**
 * This class is responsible for extracting information associated to the fiscal year from a csv row
 */
class FiscalYearParser {
    /**
     * Returns the information about the companies fiscal year (End date and whether it deviates from a normal calendar
     * year)
     */
    private val columnMappingFiscalYear = mapOf(
        "fiscalYearDeviation" to "Fiscal Year",
        "fiscalYearEnd" to "Fiscal Year End",
    )
    private val fiscalYearDeviationMap = mapOf(
        "No Deviation" to YesNo.No,
        "Deviation" to YesNo.Yes
    )
    /**
     * Method to retrieve information about the deviation of the fiscal year from the csv file
     */
    fun getFiscalYearDeviation(csvLineData: Map<String, String>): YesNo? {
        val fiscalYearDeviationString = columnMappingFiscalYear.getCsvValue("fiscalYearDeviation", csvLineData)
        return fiscalYearDeviationMap[fiscalYearDeviationString]
    }

    /**
     * Method to retrieve the end date of the fiscal year from the csv file
     */
    fun getFiscalYearEnd(csvLineData: Map<String, String>): LocalDate? {
        val fiscalYearEndString = columnMappingFiscalYear.getCsvValue("fiscalYearEnd", csvLineData)
        return if (fiscalYearEndString.isNullOrBlank())
            null
        else
            LocalDate.parse(fiscalYearEndString)
    }
}
