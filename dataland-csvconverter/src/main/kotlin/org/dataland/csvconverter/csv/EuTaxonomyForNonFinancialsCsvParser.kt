package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyTypeParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.commonfieldparsers.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.commonfieldparsers.FiscalYearParser
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDetailsPerCashFlowType

/**
 * This class contains the parsing logic for the EuTaxonomyForNonFinancials framework
 */
class EuTaxonomyForNonFinancialsCsvParser(
    private val commonFieldParser: EuTaxonomyCommonFieldParser,
    private val companyTypeParser: CompanyTypeParser,
    private val dataPointParser: DataPointParser,
    private val assuranceDataParser: AssuranceDataParser,
    private val fiscalYearParser: FiscalYearParser,
    private val companyReportParser: CompanyReportParser,

) : CsvFrameworkParser<EuTaxonomyDataForNonFinancials> {

    private val columnMappingEuTaxonomyForNonFinancials = mapOf(
        "totalRevenue" to "Total Revenue",
        "totalCapex" to "Total CapEx",
        "totalOpex" to "Total OpEx",
        "eligibleRevenue" to "Eligible Revenue",
        "eligibleCapex" to "Eligible CapEx",
        "eligibleOpex" to "Eligible OpEx",
        "alignedRevenue" to "Aligned Revenue",
        "alignedCapex" to "Aligned CapEx",
        "alignedOpex" to "Aligned OpEx",
    )

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return companyTypeParser.getCompanyType(row) == "IS"
    }

    @Suppress("kotlin:S138")
    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            totalAmount =
            dataPointParser.buildDecimalDataPoint(
                columnMappingEuTaxonomyForNonFinancials,
                csvLineData,
                "total$type",
                CsvUtils.SCALE_FACTOR_ONE_MILLION,
            ),
            alignedPercentage =
            dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForNonFinancials,
                csvLineData,
                "aligned$type",
            ),
            eligiblePercentage =
            dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForNonFinancials,
                csvLineData,
                "eligible$type",
            ),
        )
    }

    /**
     Assembles all partial information into one EuTaxonomyDataForNonFinancials object
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForNonFinancials {
        return EuTaxonomyDataForNonFinancials(
            capex = buildEuTaxonomyDetailsPerCashFlowType("Capex", row),
            opex = buildEuTaxonomyDetailsPerCashFlowType("Opex", row),
            revenue = buildEuTaxonomyDetailsPerCashFlowType("Revenue", row),
            fiscalYearDeviation = fiscalYearParser.getFiscalYearDeviation(row),
            fiscalYearEnd = fiscalYearParser.getFiscalYearEnd(row),
            scopeOfEntities = commonFieldParser.getScopeOfEntities(row),
            reportingObligation = commonFieldParser.getReportingObligation(row),
            activityLevelReporting = commonFieldParser.getActivityLevelReporting(row),
            assurance = assuranceDataParser.buildSingleAssuranceData(row),
            numberOfEmployees = commonFieldParser.getNumberOfEmployees(row),
            referencedReports = companyReportParser.buildMapOfAllCompanyReports(row),
        )
    }
}
