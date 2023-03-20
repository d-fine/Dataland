package org.dataland.csvconverter

import org.dataland.csvconverter.csv.CompanyInformationCsvParser
import org.dataland.csvconverter.csv.CsvFrameworkParser
import org.dataland.csvconverter.csv.CsvUtils
import org.dataland.csvconverter.csv.EuTaxonomyForFinancialsCsvParser
import org.dataland.csvconverter.csv.EuTaxonomyForNonFinancialsCsvParser
import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyTypeParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.commonfieldparsers.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.commonfieldparsers.FiscalYearParser
import org.dataland.csvconverter.csv.commonfieldparsers.ReportingPeriodParser
import org.dataland.csvconverter.csv.utils.YesNoNaParser
import org.dataland.csvconverter.csv.utils.YesNoParser
import org.dataland.csvconverter.json.JsonConfig
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.utils.CompanyInformationWithData
import java.io.File

/**
 * Class to transform company information and EU Taxonomy data for non financials delivered by csv into json format
 */
class CsvToJsonConverter {

    private var rawCsvData: List<Map<String, String>> = listOf()

    private val companyParser = CompanyInformationCsvParser()
    private val euTaxonomyCommonFieldParser = EuTaxonomyCommonFieldParser(
        YesNoNaParser(),
        YesNoParser(),
    )
    private val companyTypeParser = CompanyTypeParser()
    private val fiscalYearParser = FiscalYearParser()
    private val companyReportParser = CompanyReportParser(YesNoNaParser())
    private val dataPointParser = DataPointParser(companyReportParser)
    private val assuranceDataParser = AssuranceDataParser(dataPointParser)
    private val euTaxonomyForFinancialsCsvParser = EuTaxonomyForFinancialsCsvParser(
        euTaxonomyCommonFieldParser,
        companyTypeParser,
        dataPointParser,
        assuranceDataParser,
        fiscalYearParser,
        companyReportParser,
    )
    private val euTaxonomyForNonFinancialsCsvParser = EuTaxonomyForNonFinancialsCsvParser(
        euTaxonomyCommonFieldParser,
        companyTypeParser,
        dataPointParser,
        assuranceDataParser,
        fiscalYearParser,
        companyReportParser,
    )
    private val reportingPeriodParser = ReportingPeriodParser()

    /**
     * Function to parse company-associated framework data from a CSV file
     */
    private fun <T> buildListOfCompanyInformationWithFrameworkData(
        csv: List<Map<String, String>>,
        dataParser: CsvFrameworkParser<T>,
    ): List<CompanyInformationWithData<T>> {
        return csv
            .mapNotNull {
                val company = companyParser.buildCompanyInformation(it)
                if (dataParser.validateLine(company, it)) {
                    CompanyInformationWithData(
                        company,
                        dataParser.buildData(it),
                        reportingPeriodParser.getReportingPeriod(it),
                    )
                } else {
                    null
                }
            }
    }

    /**
     * Parses data for the EuTaxonomyForNonFinancials framework from the CSV
     */
    fun parseEuTaxonomyNonFinancialData(): List<CompanyInformationWithData<EuTaxonomyDataForNonFinancials>> =
        buildListOfCompanyInformationWithFrameworkData(rawCsvData, euTaxonomyForNonFinancialsCsvParser)

    /**
     * Parses data for the EuTaxonomyForFinancials framework from the CSV
     */
    fun parseEuTaxonomyFinancialData(): List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> =
        buildListOfCompanyInformationWithFrameworkData(rawCsvData, euTaxonomyForFinancialsCsvParser)

    /**
     * Method to read a given csv file
     */
    fun parseCsvFile(filePath: String) {
        rawCsvData = CsvUtils.readCsvFile(filePath)
        rawCsvData = rawCsvData.map { row -> row.mapKeys { it.key.lowercase() } }
    }

    companion object {
        /**
         * The corresponding main class to run the CSV converter. Execute by running:
         * "./gradlew :dataland-csvconverter:run --args="<FileLocation>"
         * where <FileLocation> is the location of the CSV file to be converted
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val converter = CsvToJsonConverter()
            converter.parseCsvFile(File(args.first()).path)

            val euTaxonomyNonFinancial = converter.parseEuTaxonomyNonFinancialData()
            JsonConfig.exportJson("./CompanyInformationWithEuTaxonomyDataForNonFinancials.json", euTaxonomyNonFinancial)

            val euTaxonomyFinancial = converter.parseEuTaxonomyFinancialData()
            JsonConfig.exportJson("./CompanyInformationWithEuTaxonomyDataForFinancials.json", euTaxonomyFinancial)
        }
    }
}
