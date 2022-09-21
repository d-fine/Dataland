package org.dataland.csvconverter.csv

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDetailsPerCashFlowType

/**
 * This class contains the parsing logic for the EuTaxonomyForNonFinancials framework
 */
class EuTaxonomyForNonFinancialsCsvParser(
    private val commonFieldParser: EuTaxonomyCommonFieldParser
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
        "companyType" to "IS/FS",
    )

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return commonFieldParser.getCompanyType(row) == "IS"
    }

    /**
     * Method to build EuTaxonomyDataForNonFinancials from the read row in the csv file.
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForNonFinancials {
        return EuTaxonomyDataForNonFinancials(
            reportingObligation = commonFieldParser.getReportingObligation(row),
            assurance = commonFieldParser.buildSingleAssuranceData(row),
            capex = buildEuTaxonomyDetailsPerCashFlowType("Capex", row),
            opex = buildEuTaxonomyDetailsPerCashFlowType("Opex", row),
            revenue = buildEuTaxonomyDetailsPerCashFlowType("Revenue", row)
        )
    }

    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            totalAmount =
            commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "total$type"),
            alignedPercentage =
            commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "aligned$type"),
            eligiblePercentage =
            commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForNonFinancials, csvLineData, "eligible$type")
        )
    }
}
