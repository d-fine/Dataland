package org.dataland.csvconverter

import org.dataland.csvconverter.csv.CompanyInformationCsvParser
import org.dataland.csvconverter.csv.CsvFrameworkParser
import org.dataland.csvconverter.csv.CsvUtils
import org.dataland.csvconverter.csv.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.EuTaxonomyForFinancialsCsvParser
import org.dataland.csvconverter.csv.EuTaxonomyForNonFinancialsCsvParser
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
    private val euTaxonomyCommonFieldParser = EuTaxonomyCommonFieldParser()
    private val euTaxonomyForFinancialsCsvParser = EuTaxonomyForFinancialsCsvParser(euTaxonomyCommonFieldParser)
    private val euTaxonomyForNonFinancialsCsvParser = EuTaxonomyForNonFinancialsCsvParser(euTaxonomyCommonFieldParser)

    /**
     * Function to parse company-associated framework data from a CSV file
     */
    private fun <T> buildListOfCompanyInformationWithFrameworkData(
        csv: List<Map<String, String>>,
        companyParser: CompanyInformationCsvParser,
        dataParser: CsvFrameworkParser<T>
    ): List<CompanyInformationWithData<T>> {
        return csv
            .mapNotNull {
                val company = companyParser.buildCompanyInformation(it)
                if (dataParser.validateLine(company, it)) {
                    CompanyInformationWithData(
                        company,
                        dataParser.buildData(it)
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
        buildListOfCompanyInformationWithFrameworkData(rawCsvData, companyParser, euTaxonomyForNonFinancialsCsvParser)

    /**
     * Parses data for the EuTaxonomyForFinancials framework from the CSV
     */
    fun parseEuTaxonomyFinancialData(): List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> =
        buildListOfCompanyInformationWithFrameworkData(rawCsvData, companyParser, euTaxonomyForFinancialsCsvParser)

    /**
     * Method to read a given csv file
     */
    fun parseCsvFile(filePath: String) {
        rawCsvData = CsvUtils.readCsvFile(filePath)
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
