package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CompanyInformationCsvParser.Companion.companyInformationColumnMapping
import org.dataland.csvconverter.csv.CsvUtils.NOT_AVAILABLE_STRING
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.csvconverter.csv.EuTaxonomyUtils.getAttestation
import org.dataland.csvconverter.csv.EuTaxonomyUtils.getReportingObligation
import org.dataland.datalandbackend.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.model.EuTaxonomyDetailsPerCashFlowType

/**
 * This class contains the parsing logic for the eu-taxonomy-non-financials framework
 */
class EuTaxonomyForNonFinancialsCsvParser : CsvFrameworkParser<EuTaxonomyDataForNonFinancials> {

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

    override fun validateLine(row: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return companyInformationColumnMapping.getCsvValue("companyType", row) !in listOf("FS", NOT_AVAILABLE_STRING) &&
            companyInformationColumnMapping.getCsvValue("marketCap", row) != NOT_AVAILABLE_STRING
    }

    /**
     * Method to build EuTaxonomyDataForNonFinancials from the read row in the csv file.
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForNonFinancials {
        return EuTaxonomyDataForNonFinancials(
            reportObligation = getReportingObligation(row),
            attestation = getAttestation(row),
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
