package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyTypeParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.commonfieldparsers.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.commonfieldparsers.FiscalYearParser
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
    private val commonFieldParser: EuTaxonomyCommonFieldParser,
    private val companyTypeParser: CompanyTypeParser,
    private val dataPointParser: DataPointParser,
    private val assuranceDataParser: AssuranceDataParser,
    private val fiscalYearParser: FiscalYearParser,
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
    )

    private val columnMappingCompanyType = mapOf(
            FinancialServicesType.CreditInstitution to "Credit Institution",
            FinancialServicesType.InvestmentFirm to "Investment Firm",
            FinancialServicesType.AssetManagement to "Asset Management Company",
            FinancialServicesType.InsuranceOrReinsurance to "Insurance/Reinsurance",
    )

    private val financialServicesMap = mapOf<FinancialServicesType, String>(
            FinancialServicesType.CreditInstitution to "1",
            FinancialServicesType.InsuranceOrReinsurance to "2",
            FinancialServicesType.AssetManagement to "3",
            FinancialServicesType.InvestmentFirm to "4"
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
                    candidate.equals(financialServicesMap[it], ignoreCase = true)
                } ?: throw IllegalArgumentException("Could not determine financial services type")
            }
        )
    }

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return companyTypeParser.getCompanyType(row) == "FS"
    }

    /**
     * Callable function generating the string-maps for the Eligibility KPIs for all Financial Service Types
     */
    private fun buildEligibilityColumnMapping(type: FinancialServicesType): Map<String, String> {
        return mapOf(
            "investmentNonNfrd" to
                "Exposures to non-NFRD entities ${columnMappingCompanyType[type]}",
            "taxonomyEligibleActivity" to
                "Exposures to taxonomy-eligible economic activities" +
                " ${columnMappingCompanyType[type]}",
            "taxonomyNonEligibleActivity" to
                "Exposures to taxonomy non-eligible economic activities" +
                " ${columnMappingCompanyType[type]}",
            "banksAndIssuers" to
                "Exposures to central governments, central banks, supranational issuers" +
                " ${columnMappingCompanyType[type]}",
            "derivatives" to
                "Exposures to derivatives ${columnMappingCompanyType[type]}",
        )
    }

    /**
     * Callable functions assembling the different types of KPIs
     */

    private fun buildSingleEligibilityKpis(row: Map<String, String>, type: FinancialServicesType): EligibilityKpis {
        return EligibilityKpis(
            taxonomyEligibleActivity = dataPointParser.buildSingleDataPoint(
                buildEligibilityColumnMapping(type), row,
                "taxonomyEligibleActivity"
            ),
            taxonomyNonEligibleActivity = dataPointParser.buildSingleDataPoint(
                buildEligibilityColumnMapping(type), row,
                "taxonomyNonEligibleActivity"
            ),
            banksAndIssuers = dataPointParser.buildSingleDataPoint(
                buildEligibilityColumnMapping(type), row,
                "banksAndIssuers"
            ),
            derivatives = dataPointParser.buildSingleDataPoint(
                buildEligibilityColumnMapping(type), row,
                "derivatives"
            ),
            investmentNonNfrd = dataPointParser.buildSingleDataPoint(
                buildEligibilityColumnMapping(type), row,
                "investmentNonNfrd"
            ),
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
            tradingPortfolio = dataPointParser.buildSingleDataPoint(
                columnMappingEuTaxonomyForFinancials, row,
                "tradingPortfolio"
            ),
            interbankLoans = dataPointParser.buildSingleDataPoint(
                columnMappingEuTaxonomyForFinancials, row,
                "interbankLoans"
            ),
            tradingPortfolioAndInterbankLoans = dataPointParser.buildSingleDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "tradingPortfolioAndInterbankLoans"
            ),
        )
    }

    private fun buildInsuranceKpis(row: Map<String, String>): InsuranceKpis {
        return InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities = dataPointParser.buildSingleDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "taxonomyEligibleNonLifeInsuranceActivities"
            ),
        )
    }
    /**
     Assembles all partial information into one EuTaxonomyDataForFinancials object
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForFinancials {
        val financialServicesTypes = getFinancialServiceTypes(row)
        return EuTaxonomyDataForFinancials(
            eligibilityKpis = buildEligibilityKpis(row, financialServicesTypes),
            creditInstitutionKpis = buildCreditInstitutionKpis(row),
            insuranceKpis = buildInsuranceKpis(row),
            fiscalYearDeviation = fiscalYearParser.getFiscalYearDeviation(row),
            fiscalYearEnd = fiscalYearParser.getFiscalYearEnd(row),
            scopeOfEntities = commonFieldParser.getScopeOfEntities(row),
            reportingObligation = commonFieldParser.getReportingObligation(row),
            activityLevelReporting = commonFieldParser.getActivityLevelReporting(row),
            assurance = assuranceDataParser.buildSingleAssuranceData(row),
        )
    }
}
