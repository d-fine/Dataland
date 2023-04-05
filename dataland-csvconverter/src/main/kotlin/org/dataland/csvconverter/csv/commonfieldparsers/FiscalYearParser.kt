package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
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
    private val fiscalYearDeviationParser = EnumCsvParser(
        mapOf(
            "No Deviation" to FiscalYearDeviation.NoDeviation,
            "Deviation" to FiscalYearDeviation.Deviation,
        ),
    )

    /**
     * Method to retrieve information about the deviation of the fiscal year from the csv file
     */
    fun getFiscalYearDeviation(csvLineData: Map<String, String>): FiscalYearDeviation {
        return columnMappingFiscalYear.getCsvValue("fiscalYearDeviation", csvLineData)
            .let { fiscalYearDeviationParser.parse("fiscalYearDeviation", it) }
    }

    /**
     * Method to retrieve the end date of the fiscal year from the csv file
     */
    fun getFiscalYearEnd(csvLineData: Map<String, String>): LocalDate {
        val fiscalYearEndString = columnMappingFiscalYear.getCsvValue("fiscalYearEnd", csvLineData)
        return LocalDate.parse(fiscalYearEndString)
    }
}
