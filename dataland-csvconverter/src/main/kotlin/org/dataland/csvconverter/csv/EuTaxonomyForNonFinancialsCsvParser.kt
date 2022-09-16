package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
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
        "totalRevenue" to "Total Revenue EURmm",
        "totalCapex" to "Total CapEx EURmm",
        "totalOpex" to "Total OpEx EURmm",
        "eligibleRevenue" to "Eligible Revenue",
        "eligibleCapex" to "Eligible CapEx",
        "eligibleOpex" to "Eligible OpEx",
        "alignedRevenue" to "Aligned Revenue",
        "alignedCapex" to "Aligned CapEx",
        "alignedOpex" to "Aligned OpEx",
        "companyType" to "IS/FS"
    )

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return commonFieldParser.getCompanyType(row) == "IS"
    }

    /**
     * Method to build EuTaxonomyDataForNonFinancials from the read row in the csv file.
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForNonFinancials {
        return EuTaxonomyDataForNonFinancials(
            reportObligation = commonFieldParser.getReportingObligation(row),
            assurance = commonFieldParser.getAttestation(row),
            capex = buildEuTaxonomyDetailsPerCashFlowType("Capex", row),
            opex = buildEuTaxonomyDetailsPerCashFlowType("Opex", row),
            revenue = buildEuTaxonomyDetailsPerCashFlowType("Revenue", row)
        )
    }

    private fun buildEuTaxonomyDetailsPerCashFlowType(type: String, csvLineData: Map<String, String>):
        EuTaxonomyDetailsPerCashFlowType {
        return EuTaxonomyDetailsPerCashFlowType(
            totalAmount =
            columnMappingEuTaxonomyForNonFinancials.getNumericCsvValue("total$type", csvLineData),
            alignedPercentage =
            columnMappingEuTaxonomyForNonFinancials.getNumericCsvValue("aligned$type", csvLineData),
            eligiblePercentage =
            columnMappingEuTaxonomyForNonFinancials.getNumericCsvValue("eligible$type", csvLineData)
        )
    }
}
