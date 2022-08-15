package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CompanyInformationCsvParser.Companion.companyInformationColumnMapping
import org.dataland.csvconverter.csv.CsvUtils.NOT_AVAILABLE_STRING
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.csvconverter.csv.EuTaxonomyUtils.getAttestation
import org.dataland.csvconverter.csv.EuTaxonomyUtils.getReportingObligation
import org.dataland.datalandbackend.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.enums.eutaxonomy.FinancialServicesType

/**
 * This class contains the parsing logic for the eu-taxonomy-for-financials framework
 */
class EuTaxonomyForFinancialsCsvParser : CsvFrameworkParser<EuTaxonomyDataForFinancials> {

    private val columnMappingEuTaxonomyForFinancials = mapOf(
        "financialServicesType" to "FS - company type",
        "taxonomyEligibleActivity" to "Exposures to taxonomy-eligible economic activities",
        "banksAndIssuers" to "Exposures to central governments, central banks, supranational issuers",
        "derivatives" to "Exposures to derivatives",
        "investmentNonNfrd" to "Exposures to non-NFRD entities",
        "tradingPortfolio" to "Trading portfolio",
        "interbankLoans" to "On-demand interbank loans",
        "tradingPortfolio" to "Trading portfolio",
        "tradingPortfolioAndInterbankLoans" to "Trading portfolio & on demand interbank loans",
        "taxonomyEligibleNonLifeInsuranceActivities" to "Taxonomy-eligible non-life insurance economic activities",
        FinancialServicesType.CreditInstitution.name to "Credit Institution",
        FinancialServicesType.AssetManagement.name to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance.name to "Insurance/Reinsurance"
    )

    private fun getFinancialServiceType(csvLineData: Map<String, String>): FinancialServicesType {
        return FinancialServicesType.values().firstOrNull {
            csvLineData[columnMappingEuTaxonomyForFinancials["financialServicesType"]]
                .equals(columnMappingEuTaxonomyForFinancials[it.name], ignoreCase = true)
        } ?: throw IllegalArgumentException("Could not determine financial services type")
    }

    override fun validateLine(row: Map<String, String>): Boolean {
        // Skip all lines with financial companies or without market cap
        return companyInformationColumnMapping.getCsvValue("companyType", row) == "FS" &&
            companyInformationColumnMapping.getCsvValue("marketCap", row) != NOT_AVAILABLE_STRING &&
            // Skip Allianz until inconsistencies are resolved
            companyInformationColumnMapping.getCsvValue("companyName", row).trim() != "Allianz SE"
    }

    private fun buildEligibilityKpis(row: Map<String, String>): EuTaxonomyDataForFinancials.EligibilityKpis {
        return EuTaxonomyDataForFinancials.EligibilityKpis(
            taxonomyEligibleActivity =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("taxonomyEligibleActivity", row),
            banksAndIssuers =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("banksAndIssuers", row),
            derivatives =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("derivatives", row),
            investmentNonNfrd =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("investmentNonNfrd", row),
        )
    }

    private fun buildCreditInstitutionKpis(
        row: Map<String, String>
    ): EuTaxonomyDataForFinancials.CreditInstitutionKpis {
        return EuTaxonomyDataForFinancials.CreditInstitutionKpis(
            interbankLoans =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("interbankLoans", row),
            tradingPortfolio =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("tradingPortfolio", row),
            tradingPortfolioAndInterbankLoans =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("tradingPortfolioAndInterbankLoans", row)
        )
    }

    private fun buildInsuranceKpis(row: Map<String, String>): EuTaxonomyDataForFinancials.InsuranceKpis {
        return EuTaxonomyDataForFinancials.InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue(
                "taxonomyEligibleNonLifeInsuranceActivities",
                row
            ),
        )
    }

    override fun buildData(row: Map<String, String>): EuTaxonomyDataForFinancials {
        return EuTaxonomyDataForFinancials(
            reportingObligation = getReportingObligation(row),
            attestation = getAttestation(row),
            financialServicesType = getFinancialServiceType(row),
            eligibilityKpis = buildEligibilityKpis(row),
            creditInstitutionKpis = buildCreditInstitutionKpis(row),
            insuranceKpis = buildInsuranceKpis(row),
        )
    }
}
