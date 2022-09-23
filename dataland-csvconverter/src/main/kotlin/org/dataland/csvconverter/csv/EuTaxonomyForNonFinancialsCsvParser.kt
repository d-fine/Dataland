package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.commonfieldparsers.*
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

    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            totalAmount =
            dataPointParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "total$type"),
            alignedPercentage =
            dataPointParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "aligned$type"),
            eligiblePercentage =
            dataPointParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "eligible$type")
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
        )
    }
}
