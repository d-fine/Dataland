package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getNumericCsvValue
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.financials.CreditInstitutionKpis
import org.dataland.datalandbackend.model.eutaxonomy.financials.EligibilityKpis
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.eutaxonomy.financials.InsuranceKpis
import java.util.EnumSet

/**
 * This class contains the parsing logic for the EuTaxonomyForFinancials framework
 */
class EuTaxonomyForFinancialsCsvParser(
    private val commonFieldParser: EuTaxonomyCommonFieldParser
) : CsvFrameworkParser<EuTaxonomyDataForFinancials> {

    private val columnMappingEuTaxonomyForFinancials = mapOf(
        "financialServicesType" to "FS - company type",
        "tradingPortfolio" to "Trading portfolio",
        "interbankLoans" to "On-demand interbank loans",
        "tradingPortfolio" to "Trading portfolio",
        "tradingPortfolioAndInterbankLoans" to "Trading portfolio & on demand interbank loans",
        "taxonomyEligibleNonLifeInsuranceActivities" to "Taxonomy-eligible non-life insurance economic activities",
        FinancialServicesType.CreditInstitution.name to "Credit Institution",
        FinancialServicesType.AssetManagement.name to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance.name to "Insurance/Reinsurance"
    )

    private fun getFinancialServiceTypes(csvLineData: Map<String, String>): EnumSet<FinancialServicesType> {
        val csvData = csvLineData[columnMappingEuTaxonomyForFinancials["financialServicesType"]]!!
        val split = csvData.split(",").map { it.trim() }
        return EnumSet.copyOf(
            split.map {
                    candidate ->
                FinancialServicesType.values().firstOrNull {
                    candidate.equals(columnMappingEuTaxonomyForFinancials[it.name], ignoreCase = true)
                } ?: throw IllegalArgumentException("Could not determine financial services type")
            }
        )
    }

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return commonFieldParser.getCompanyType(row) == "FS"
    }

    private fun buildEligibilityColumnMapping(type: FinancialServicesType): Map<String, String> {
        return mapOf(
            "investmentNonNfrd" to "Exposures to non-NFRD entities ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "taxonomyEligibleActivity" to "Exposures to taxonomy-eligible economic activities" +
                " ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "banksAndIssuers" to "Exposures to central governments, central banks, supranational issuers" +
                " ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "derivatives" to "Exposures to derivatives ${columnMappingEuTaxonomyForFinancials[type.name]}",
        )
    }

    private fun buildSingleEligibilityKpis(row: Map<String, String>, type: FinancialServicesType): EligibilityKpis {
        return EligibilityKpis(
            taxonomyEligibleActivity =
            buildEligibilityColumnMapping(type).getNumericCsvValue("taxonomyEligibleActivity", row),
            banksAndIssuers =
            buildEligibilityColumnMapping(type).getNumericCsvValue("banksAndIssuers", row),
            derivatives =
            buildEligibilityColumnMapping(type).getNumericCsvValue("derivatives", row),
            investmentNonNfrd =
            buildEligibilityColumnMapping(type).getNumericCsvValue("investmentNonNfrd", row),
        )
    }

    private fun buildEligibilityKpis(
        row: Map<String, String>,
        types: EnumSet<FinancialServicesType>
    ): Map<FinancialServicesType, EligibilityKpis> {
        return types.associateWith { buildSingleEligibilityKpis(row, it) }
    }

    private fun buildCreditInstitutionKpis(
        row: Map<String, String>
    ): CreditInstitutionKpis {
        return CreditInstitutionKpis(
            interbankLoans =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("interbankLoans", row),
            tradingPortfolio =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("tradingPortfolio", row),
            tradingPortfolioAndInterbankLoans =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue("tradingPortfolioAndInterbankLoans", row)
        )
    }

    private fun buildInsuranceKpis(row: Map<String, String>): InsuranceKpis {
        return InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities =
            columnMappingEuTaxonomyForFinancials.getNumericCsvValue(
                "taxonomyEligibleNonLifeInsuranceActivities",
                row
            ),
        )
    }

    override fun buildData(row: Map<String, String>): EuTaxonomyDataForFinancials {
        val financialServicesTypes = getFinancialServiceTypes(row)
        return EuTaxonomyDataForFinancials(
            reportingObligation = commonFieldParser.getReportingObligation(row),
            attestation = commonFieldParser.getAttestation(row),
            financialServicesTypes = financialServicesTypes,
            eligibilityKpis = buildEligibilityKpis(row, financialServicesTypes),
            creditInstitutionKpis = buildCreditInstitutionKpis(row),
            insuranceKpis = buildInsuranceKpis(row),
        )
    }
}
