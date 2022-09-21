package org.dataland.csvconverter.csv

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

    /**
     * general string Mappings
     */
    private val columnMappingEuTaxonomyForFinancials = mapOf(
        "financialServicesType" to "FS - company type",
        "tradingPortfolio" to "Trading portfolio",
        "interbankLoans" to "On-demand interbank loans",
        "tradingPortfolioAndInterbankLoans" to "Trading portfolio & on demand interbank loans",
        "taxonomyEligibleNonLifeInsuranceActivities" to "Taxonomy-eligible non-life insurance economic activities",
        FinancialServicesType.CreditInstitution.name to "Credit Institution",
        FinancialServicesType.AssetManagement.name to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance.name to "Insurance/Reinsurance",
        FinancialServicesType.InvestmentFirm.name to "Investment Firm"
    )

    /**
     * Function retrieving all Financial Service types of the company
     */
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

    /**
     * Callable function generating the string-maps for the Eligibility KPIs for all Financial Service Types
     */
    private fun buildEligibilityColumnMapping(type: FinancialServicesType): Map<String, String> {
        return mapOf(
            "investmentNonNfrd" to
                "Exposures to non-NFRD entities ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "taxonomyEligibleActivity" to
                "Exposures to taxonomy-eligible economic activities" +
                " ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "taxonomyNonEligibleActivity" to
                "Exposures to taxonomy non-eligible economic activities" +
                " ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "banksAndIssuers" to
                "Exposures to central governments, central banks, supranational issuers" +
                " ${columnMappingEuTaxonomyForFinancials[type.name]}",
            "derivatives" to
                "Exposures to derivatives ${columnMappingEuTaxonomyForFinancials[type.name]}",
        )
    }

    private fun buildSingleEligibilityKpis(row: Map<String, String>, type: FinancialServicesType): EligibilityKpis {
        return EligibilityKpis(
            taxonomyEligibleActivity = commonFieldParser.buildSingleDataPoint(buildEligibilityColumnMapping(type), row, "taxonomyEligibleActivity"),
            taxonomyNonEligibleActivity = commonFieldParser.buildSingleDataPoint(buildEligibilityColumnMapping(type), row, "taxonomyNonEligibleActivity"),
            banksAndIssuers = commonFieldParser.buildSingleDataPoint(buildEligibilityColumnMapping(type), row, "banksAndIssuers"),
            derivatives = commonFieldParser.buildSingleDataPoint(buildEligibilityColumnMapping(type), row, "derivatives"),
            investmentNonNfrd = commonFieldParser.buildSingleDataPoint(buildEligibilityColumnMapping(type), row, "investmentNonNfrd"),
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
            tradingPortfolio = commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForFinancials, row, "tradingPortfolio"),
            interbankLoans = commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForFinancials, row, "interbankLoans"),
            tradingPortfolioAndInterbankLoans = commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForFinancials, row, "tradingPortfolioAndInterbankLoans"),
        )
    }

    private fun buildInsuranceKpis(row: Map<String, String>): InsuranceKpis {
        return InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities = commonFieldParser.buildSingleDataPoint(columnMappingEuTaxonomyForFinancials, row, "taxonomyEligibleNonLifeInsuranceActivities"),
        )
    }

    override fun buildData(row: Map<String, String>): EuTaxonomyDataForFinancials {
        val financialServicesTypes = getFinancialServiceTypes(row)
        return EuTaxonomyDataForFinancials(
            reportingObligation = commonFieldParser.getReportingObligation(row),
            assurance = commonFieldParser.buildSingleAssuranceData(row),
            financialServicesTypes = financialServicesTypes,
            eligibilityKpis = buildEligibilityKpis(row, financialServicesTypes),
            creditInstitutionKpis = buildCreditInstitutionKpis(row),
            insuranceKpis = buildInsuranceKpis(row),
        )
    }
}
